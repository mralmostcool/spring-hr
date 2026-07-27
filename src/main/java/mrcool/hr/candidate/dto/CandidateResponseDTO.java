package mrcool.hr.candidate.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CandidateResponseDTO(
        UUID id,
        String name,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
