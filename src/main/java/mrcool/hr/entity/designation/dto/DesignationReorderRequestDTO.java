package mrcool.hr.entity.designation.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record DesignationReorderRequestDTO(
        @NotNull UUID id,
        @NotNull Integer rank) {
}
