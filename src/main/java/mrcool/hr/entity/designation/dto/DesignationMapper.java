package mrcool.hr.entity.designation.dto;

import mrcool.hr.entity.designation.Designation;

public class DesignationMapper {

    public static DesignationResponseDTO toResponse(Designation designation) {
        return new DesignationResponseDTO(
                designation.getId(),
                designation.getName(),
                designation.getRank(),
                designation.getCreatedAt(),
                designation.getUpdatedAt());
    }

    public static Designation toEntity(DesignationRequestDTO request) {
        return Designation.builder()
                .name(request.name())
                .rank(request.rank())
                .build();
    }

}
