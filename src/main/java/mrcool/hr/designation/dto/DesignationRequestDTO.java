package mrcool.hr.designation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// import mrcool.hr.designation.Designation;

public record DesignationRequestDTO(
        @NotBlank @NotNull @Size(max = 255) String name) {
    // public Designation toEntity() {
    // return Designation.builder().name(name).build();
    // }
}