package mrcool.hr.employee.dto;

import mrcool.hr.designation.Designation;
import mrcool.hr.designation.dto.DesignationMapper;
import mrcool.hr.employee.Employee;

public class EmployeeMapper {

    public static EmployeeResponseDTO toResponse(Employee employee) {
        return new EmployeeResponseDTO(
                employee.getId(),
                employee.getName(),
                employee.getDesignation() != null ? DesignationMapper.toResponse(employee.getDesignation()) : null,
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }

    public static Employee toEntity(EmployeeRequestDTO request, Designation designation) {
        return Employee.builder()
                .name(request.name())
                .designation(designation)
                .build();
    }
}
