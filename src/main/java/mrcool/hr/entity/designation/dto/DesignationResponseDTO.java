package mrcool.hr.entity.designation.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

// import mrcool.hr.entity.designation.Designation;

public record DesignationResponseDTO(
        UUID id,
        String name,
        Integer rank,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
    public DesignationResponseDTO(UUID id, String name, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this(id, name, null, createdAt, updatedAt);
    }
}
