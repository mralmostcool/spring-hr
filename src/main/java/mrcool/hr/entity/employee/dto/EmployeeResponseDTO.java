package mrcool.hr.entity.employee.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import mrcool.hr.entity.designation.dto.DesignationResponseDTO;

public record EmployeeResponseDTO(
        UUID id,
        String name,
        DesignationResponseDTO designation,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
