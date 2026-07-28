package mrcool.hr.entity.candidate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CandidateRequestDTO(
                @NotBlank @NotNull @Size(max = 255) String name) {
}
