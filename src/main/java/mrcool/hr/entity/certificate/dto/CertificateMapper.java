package mrcool.hr.entity.certificate.dto;

import java.util.List;
import mrcool.hr.entity.candidate.dto.CandidateMapper;
import mrcool.hr.entity.designation.dto.DesignationMapper;
import mrcool.hr.entity.employee.dto.EmployeeMapper;
import mrcool.hr.entity.certificate.CertificateAction;
import mrcool.hr.entity.certificate.CertificateState;

public class CertificateMapper {

    public static CertificateResponseDTO toResponse(CertificateState state, List<CertificateAction> actions) {
        List<CertificateActionResponseDTO> actionDTOs = actions.stream()
                .map(CertificateMapper::toActionResponse)
                .toList();

        return new CertificateResponseDTO(
                state.getId(),
                CandidateMapper.toResponse(state.getCandidate()),
                state.getTitle(),
                state.getStatus(),
                state.getCurrentDesignation() != null ? DesignationMapper.toResponse(state.getCurrentDesignation()) : null,
                state.getCreatedAt(),
                state.getUpdatedAt(),
                actionDTOs
        );
    }

    public static CertificateActionResponseDTO toActionResponse(CertificateAction action) {
        return new CertificateActionResponseDTO(
                action.getId(),
                EmployeeMapper.toResponse(action.getEmployee()),
                DesignationMapper.toResponse(action.getDesignation()),
                action.getAction(),
                action.getComment(),
                action.getCreatedAt()
        );
    }
}

