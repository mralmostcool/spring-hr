package mrcool.hr.entity.certificate.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CertificateActionRequestDTO(
        @NotNull UUID employeeId,
        @Size(max = 4000) String comment) {
}
