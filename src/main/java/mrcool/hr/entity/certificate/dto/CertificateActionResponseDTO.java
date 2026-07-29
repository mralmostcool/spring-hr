package mrcool.hr.entity.certificate.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import mrcool.hr.entity.employee.dto.EmployeeResponseDTO;
import mrcool.hr.entity.designation.dto.DesignationResponseDTO;
import mrcool.hr.entity.certificate.CertificateActionType;

public record CertificateActionResponseDTO(
        UUID id,
        EmployeeResponseDTO employee,
        DesignationResponseDTO designation,
        CertificateActionType action,
        String comment,
        OffsetDateTime createdAt) {
}
