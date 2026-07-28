package mrcool.hr.entity.designation.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

// import mrcool.hr.entity.designation.Designation;

public record DesignationResponseDTO(
        UUID id,
        String name,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
    // public static DesignationResponseDTO from(Designation designation) {
    // return new DesignationResponseDTO(
    // designation.getId(),
    // designation.getName(),
    // designation.getCreatedAt(),
    // designation.getUpdatedAt());
    // }
}
