package mrcool.hr.entity.candidate.dto;

import mrcool.hr.entity.candidate.Candidate;

public class CandidateMapper {

    public static CandidateResponseDTO toResponse(Candidate candidate) {
        return new CandidateResponseDTO(
                candidate.getId(),
                candidate.getName(),
                candidate.getCreatedAt(),
                candidate.getUpdatedAt());
    }

    public static Candidate toEntity(CandidateRequestDTO request) {
        return Candidate.builder()
                .name(request.name())
                .build();
    }
}
