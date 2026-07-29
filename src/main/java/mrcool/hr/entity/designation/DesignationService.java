package mrcool.hr.entity.designation;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mrcool.hr.common.exception.DuplicateResourceException;
import mrcool.hr.common.exception.ResourceNotFoundException;
import mrcool.hr.entity.designation.dto.DesignationMapper;
import mrcool.hr.entity.designation.dto.DesignationRequestDTO;
import mrcool.hr.entity.designation.dto.DesignationResponseDTO;
import mrcool.hr.entity.designation.dto.DesignationReorderRequestDTO;
import mrcool.hr.entity.employee.EmployeeRepository;
import mrcool.hr.entity.certificate.CertificateActionRepository;
import mrcool.hr.entity.certificate.CertificateStateRepository;

@Service
public class DesignationService {

    private final DesignationRepository designationRepository;
    private final EmployeeRepository employeeRepository;
    private final CertificateActionRepository certificateActionRepository;
    private final CertificateStateRepository certificateStateRepository;

    public DesignationService(DesignationRepository designationRepository, 
                              EmployeeRepository employeeRepository,
                              CertificateActionRepository certificateActionRepository,
                              CertificateStateRepository certificateStateRepository) {
        this.designationRepository = designationRepository;
        this.employeeRepository = employeeRepository;
        this.certificateActionRepository = certificateActionRepository;
        this.certificateStateRepository = certificateStateRepository;
    }

    public List<DesignationResponseDTO> getAllDesignations() {
        return designationRepository
                .findAllOrderByRankAsc()
                .stream()
                .map(DesignationMapper::toResponse)
                .toList();
    }

    @Transactional
    public List<DesignationResponseDTO> reorderDesignations(List<DesignationReorderRequestDTO> reorders) {
        // Step 1: Temporarily set rank to null to avoid unique constraint violations on swap reorders
        for (DesignationReorderRequestDTO reorder : reorders) {
            Designation designation = designationRepository.findById(reorder.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Designation with ID " + reorder.id() + " not found"));
            designation.setRank(null);
            designationRepository.save(designation);
        }
        designationRepository.flush();

        // Step 2: Apply final ranks
        for (DesignationReorderRequestDTO reorder : reorders) {
            Designation designation = designationRepository.findById(reorder.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Designation with ID " + reorder.id() + " not found"));
            designation.setRank(reorder.rank());
            designationRepository.save(designation);
        }
        designationRepository.flush();

        return getAllDesignations();
    }

    public DesignationResponseDTO getDesignationById(UUID id) {
        return designationRepository.findById(id)
                .map(DesignationMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Designation with ID " + id + " not found"));
    }

    public DesignationResponseDTO createDesignation(DesignationRequestDTO request) {
        if (designationRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Designation with name " + request.name() + " already exists");
        }
        try {
            Designation designation = DesignationMapper.toEntity(request);
            if (designation.getRank() == null) {
                Integer maxRank = designationRepository.findMaxRank();
                designation.setRank(maxRank != null ? maxRank + 1 : 1);
            }
            Designation saved = designationRepository.saveAndFlush(designation);
            return DesignationMapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Designation with name " + request.name() + " already exists");
        }
    }

    public DesignationResponseDTO updateDesignation(UUID id, DesignationRequestDTO request) {
        Designation designation = designationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designation with ID " + id + " not found"));

        if (designationRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new DuplicateResourceException("Designation with name " + request.name() + " already exists");
        }

        try {
            designation.setName(request.name());
            if (request.rank() != null) {
                designation.setRank(request.rank());
            } else if (designation.getRank() == null) {
                Integer maxRank = designationRepository.findMaxRank();
                designation.setRank(maxRank != null ? maxRank + 1 : 1);
            }

            Designation updated = designationRepository.saveAndFlush(designation);
            return DesignationMapper.toResponse(updated);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Designation with name " + request.name() + " already exists");
        }
    }

    public DesignationResponseDTO deleteDesignation(UUID id) {
        Designation designation = designationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designation with ID " + id + " not found"));

        if (employeeRepository.existsByDesignationId(id)) {
            throw new DuplicateResourceException("Cannot delete designation with ID " + id
                    + " because it is currently assigned to one or more employees.");
        }

        try {
            designationRepository.deleteById(id);
            designationRepository.flush();
            return DesignationMapper.toResponse(designation);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Cannot delete designation with ID " + id
                    + " because it is currently assigned to one or more employees.");
        }
    }

    @Transactional
    public List<DesignationResponseDTO> deleteAllDesignations() {
        List<DesignationResponseDTO> designations = getAllDesignations();
        certificateActionRepository.deleteAll();
        certificateStateRepository.deleteAll();
        employeeRepository.deleteAll();
        designationRepository.deleteAll();
        return designations;
    }

}
