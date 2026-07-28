package mrcool.hr.entity.employee.dto;

import mrcool.hr.entity.designation.Designation;
import mrcool.hr.entity.designation.dto.DesignationMapper;
import mrcool.hr.entity.employee.Employee;

public class EmployeeMapper {

    public static EmployeeResponseDTO toResponse(Employee employee) {
        return new EmployeeResponseDTO(
                employee.getId(),
                employee.getName(),
                employee.getDesignation() != null ? DesignationMapper.toResponse(employee.getDesignation()) : null,
                employee.getCreatedAt(),
                employee.getUpdatedAt());
    }

    public static Employee toEntity(EmployeeRequestDTO request, Designation designation) {
        return Employee.builder()
                .name(request.name())
                .designation(designation)
                .build();
    }
}
