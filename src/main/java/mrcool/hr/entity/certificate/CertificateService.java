package mrcool.hr.entity.certificate;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import mrcool.hr.common.exception.ResourceNotFoundException;
import mrcool.hr.entity.candidate.Candidate;
import mrcool.hr.entity.candidate.CandidateRepository;
import mrcool.hr.entity.designation.Designation;
import mrcool.hr.entity.designation.DesignationRepository;
import mrcool.hr.entity.employee.Employee;
import mrcool.hr.entity.employee.EmployeeRepository;
import mrcool.hr.entity.certificate.dto.CertificateActionRequestDTO;
import mrcool.hr.entity.certificate.dto.CertificateMapper;
import mrcool.hr.entity.certificate.dto.CertificateRequestDTO;
import mrcool.hr.entity.certificate.dto.CertificateResponseDTO;

@Service
@RequiredArgsConstructor
@Transactional
public class CertificateService {

    private final CertificateStateRepository certificateStateRepository;
    private final CertificateActionRepository certificateActionRepository;
    private final CandidateRepository candidateRepository;
    private final EmployeeRepository employeeRepository;
    private final DesignationRepository designationRepository;

    public CertificateResponseDTO createNewCertificate(CertificateRequestDTO request) {
        Candidate candidate = candidateRepository.findById(request.candidateId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate with ID " + request.candidateId() + " not found"));

        List<Designation> designations = designationRepository.findAllOrderByRankAsc();
        if (designations.isEmpty()) {
            throw new InvalidCertificateWorkflowException("No designations configured in the system. Cannot start certificate workflow.");
        }

        Designation firstDesignation = null;
        for (int i = designations.size() - 1; i >= 0; i--) {
            Designation d = designations.get(i);
            if (d.getRank() != null) {
                firstDesignation = d;
                break;
            }
        }

        if (firstDesignation == null) {
            firstDesignation = designations.get(0);
        }

        String title = request.title();
        if (title == null || title.isBlank()) {
            title = "Participation Certificate";
        }

        CertificateState state = CertificateState.builder()
                .candidate(candidate)
                .title(title)
                .status(CertificateStatus.SUBMITTED)
                .currentDesignation(firstDesignation)
                .build();

        CertificateState saved = certificateStateRepository.saveAndFlush(state);
        return CertificateMapper.toResponse(saved, List.of());
    }

    @Transactional(readOnly = true)
    public List<CertificateResponseDTO> getAllCertificates() {
        return certificateStateRepository.findAll()
                .stream()
                .map(state -> {
                    List<CertificateAction> actions = certificateActionRepository.findAllByCertificateStateIdOrderByCreatedAtAsc(state.getId());
                    return CertificateMapper.toResponse(state, actions);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public CertificateResponseDTO getSpecificCertificate(UUID id) {
        CertificateState state = certificateStateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate with ID " + id + " not found"));
        List<CertificateAction> actions = certificateActionRepository.findAllByCertificateStateIdOrderByCreatedAtAsc(id);
        return CertificateMapper.toResponse(state, actions);
    }

    public CertificateResponseDTO acceptCertificate(UUID id, CertificateActionRequestDTO actionRequest) {
        CertificateState state = certificateStateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate with ID " + id + " not found"));

        if (state.getStatus() == CertificateStatus.APPROVED || state.getStatus() == CertificateStatus.REJECTED) {
            throw new InvalidCertificateWorkflowException("Certificate workflow has already completed with status " + state.getStatus());
        }

        Employee employee = employeeRepository.findById(actionRequest.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee with ID " + actionRequest.employeeId() + " not found"));

        if (employee.getDesignation() == null || employee.getDesignation().getRank() == null) {
            throw new InvalidCertificateWorkflowException("Employee must have a designation with a valid rank to perform approval.");
        }

        int employeeRank = employee.getDesignation().getRank();

        if (state.getStatus() == CertificateStatus.SUBMITTED) {
            // First approval: Give access to anyone to approve first, provided there is a higher level above them
            List<Designation> designations = designationRepository.findAllOrderByRankAsc();
            
            // Find the designation immediately above this employee (largest rank strictly less than employeeRank)
            Designation nextHigherDesignation = null;
            for (int i = designations.size() - 1; i >= 0; i--) {
                Designation d = designations.get(i);
                if (d.getRank() != null && d.getRank() < employeeRank) {
                    nextHigherDesignation = d;
                    break;
                }
            }

            if (nextHigherDesignation == null) {
                throw new InvalidCertificateWorkflowException("The first approval cannot be performed by the highest designation level (" 
                        + employee.getDesignation().getName() + "), as the second approval must come from a strictly higher level.");
            }

            // Record Action
            CertificateAction action = CertificateAction.builder()
                    .certificateState(state)
                    .employee(employee)
                    .designation(employee.getDesignation())
                    .action(CertificateActionType.APPROVE)
                    .comment(actionRequest.comment())
                    .build();

            certificateActionRepository.saveAndFlush(action);

            // Update Certificate State
            state.setStatus(CertificateStatus.LEVEL_1);
            state.setCurrentDesignation(nextHigherDesignation);
            
        } else if (state.getStatus() == CertificateStatus.LEVEL_1) {
            // Second approval: Only levels above the first approver's rank (strictly lower rank number) can approve
            List<CertificateAction> actions = certificateActionRepository.findAllByCertificateStateIdOrderByCreatedAtAsc(id);
            CertificateAction firstApproval = actions.stream()
                    .filter(a -> a.getAction() == CertificateActionType.APPROVE)
                    .findFirst()
                    .orElseThrow(() -> new InvalidCertificateWorkflowException("First approval record not found."));

            int firstApproverRank = firstApproval.getDesignation().getRank();

            if (employeeRank >= firstApproverRank) {
                throw new InvalidCertificateWorkflowException("Only employees with designation levels strictly above " 
                        + firstApproval.getDesignation().getName() + " (rank < " + firstApproverRank + ") can approve at this stage.");
            }

            // Record Action
            CertificateAction action = CertificateAction.builder()
                    .certificateState(state)
                    .employee(employee)
                    .designation(employee.getDesignation())
                    .action(CertificateActionType.APPROVE)
                    .comment(actionRequest.comment())
                    .build();

            certificateActionRepository.saveAndFlush(action);

            // Update Certificate State to fully APPROVED
            state.setStatus(CertificateStatus.APPROVED);
            state.setCurrentDesignation(null);
        }

        CertificateState updated = certificateStateRepository.saveAndFlush(state);
        List<CertificateAction> actions = certificateActionRepository.findAllByCertificateStateIdOrderByCreatedAtAsc(id);
        return CertificateMapper.toResponse(updated, actions);
    }

    public CertificateResponseDTO rejectCertificate(UUID id, CertificateActionRequestDTO actionRequest) {
        CertificateState state = certificateStateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate with ID " + id + " not found"));

        if (state.getStatus() == CertificateStatus.APPROVED || state.getStatus() == CertificateStatus.REJECTED) {
            throw new InvalidCertificateWorkflowException("Certificate workflow has already completed with status " + state.getStatus());
        }

        Employee employee = employeeRepository.findById(actionRequest.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee with ID " + actionRequest.employeeId() + " not found"));

        if (employee.getDesignation() == null || employee.getDesignation().getRank() == null) {
            throw new InvalidCertificateWorkflowException("Employee must have a designation with a valid rank to perform rejection.");
        }

        int employeeRank = employee.getDesignation().getRank();

        if (state.getStatus() == CertificateStatus.LEVEL_1) {
            // Rejection at LEVEL_1 requires same privileges as approval (must be strictly above first approver's level)
            List<CertificateAction> actions = certificateActionRepository.findAllByCertificateStateIdOrderByCreatedAtAsc(id);
            CertificateAction firstApproval = actions.stream()
                    .filter(a -> a.getAction() == CertificateActionType.APPROVE)
                    .findFirst()
                    .orElseThrow(() -> new InvalidCertificateWorkflowException("First approval record not found."));

            int firstApproverRank = firstApproval.getDesignation().getRank();

            if (employeeRank >= firstApproverRank) {
                throw new InvalidCertificateWorkflowException("Only employees with designation levels strictly above " 
                        + firstApproval.getDesignation().getName() + " (rank < " + firstApproverRank + ") can reject at this stage.");
            }
        }

        // Record Action
        CertificateAction action = CertificateAction.builder()
                .certificateState(state)
                .employee(employee)
                .designation(employee.getDesignation())
                .action(CertificateActionType.REJECT)
                .comment(actionRequest.comment())
                .build();

        certificateActionRepository.saveAndFlush(action);

        // Terminal transition to REJECTED
        state.setStatus(CertificateStatus.REJECTED);
        state.setCurrentDesignation(null);

        CertificateState updated = certificateStateRepository.saveAndFlush(state);
        List<CertificateAction> actions = certificateActionRepository.findAllByCertificateStateIdOrderByCreatedAtAsc(id);
        return CertificateMapper.toResponse(updated, actions);
    }

}

