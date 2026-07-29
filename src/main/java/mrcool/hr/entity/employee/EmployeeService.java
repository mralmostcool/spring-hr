package mrcool.hr.entity.employee;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mrcool.hr.common.exception.ResourceNotFoundException;
import mrcool.hr.entity.designation.Designation;
import mrcool.hr.entity.designation.DesignationRepository;
import mrcool.hr.entity.employee.dto.EmployeeMapper;
import mrcool.hr.entity.employee.dto.EmployeeRequestDTO;
import mrcool.hr.entity.employee.dto.EmployeeResponseDTO;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DesignationRepository designationRepository;

    public EmployeeService(EmployeeRepository employeeRepository, DesignationRepository designationRepository) {
        this.employeeRepository = employeeRepository;
        this.designationRepository = designationRepository;
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeRepository.findAllByIsActiveTrue()
                .stream()
                .map(EmployeeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployeeById(UUID id) {
        return employeeRepository.findByIdAndIsActiveTrue(id)
                .map(EmployeeMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Employee with ID " + id + " not found"));
    }

    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO request) {
        Designation designation = designationRepository.findById(request.designationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Designation with ID " + request.designationId() + " not found"));

        Employee employee = EmployeeMapper.toEntity(request, designation);
        Employee saved = employeeRepository.save(employee);
        return EmployeeMapper.toResponse(saved);
    }

    public EmployeeResponseDTO updateEmployee(UUID id, EmployeeRequestDTO request) {
        Employee employee = employeeRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee with ID " + id + " not found"));

        Designation designation = designationRepository.findById(request.designationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Designation with ID " + request.designationId() + " not found"));

        employee.setName(request.name());
        employee.setDesignation(designation);

        Employee updated = employeeRepository.saveAndFlush(employee);
        return EmployeeMapper.toResponse(updated);
    }

    public EmployeeResponseDTO deleteEmployee(UUID id) {
        Employee employee = employeeRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee with ID " + id + " not found"));

        employee.setIsActive(false);
        Employee updated = employeeRepository.saveAndFlush(employee);
        return EmployeeMapper.toResponse(updated);
    }

    public List<EmployeeResponseDTO> deleteAllEmployees() {
        List<Employee> activeEmployees = employeeRepository.findAllByIsActiveTrue();
        for (Employee emp : activeEmployees) {
            emp.setIsActive(false);
        }
        employeeRepository.saveAllAndFlush(activeEmployees);
        return activeEmployees.stream()
                .map(EmployeeMapper::toResponse)
                .toList();
    }
}
