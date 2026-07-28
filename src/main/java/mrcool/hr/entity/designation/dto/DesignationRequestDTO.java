package mrcool.hr.entity.designation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// import mrcool.hr.entity.designation.Designation;

public record DesignationRequestDTO(
        @NotBlank @NotNull @Size(max = 255) String name,
        Integer rank) {
    public DesignationRequestDTO(String name) {
        this(name, null);
    }
}