package mrcool.hr.entity.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record EmployeeRequestDTO(
                @NotBlank @NotNull @Size(max = 255) String name,
                @NotNull UUID designationId) {
}
