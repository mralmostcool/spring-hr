package mrcool.hr.entity.certificate.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.List;
import mrcool.hr.entity.candidate.dto.CandidateResponseDTO;
import mrcool.hr.entity.designation.dto.DesignationResponseDTO;
import mrcool.hr.entity.certificate.CertificateStatus;

public record CertificateResponseDTO(
        UUID id,
        CandidateResponseDTO candidate,
        String title,
        CertificateStatus status,
        DesignationResponseDTO currentDesignation,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<CertificateActionResponseDTO> actions) {
}

