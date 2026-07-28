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
        return employeeRepository.findAll()
                .stream()
                .map(EmployeeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployeeById(UUID id) {
        return employeeRepository.findById(id)
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
        Employee employee = employeeRepository.findById(id)
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
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee with ID " + id + " not found"));

        employeeRepository.deleteById(id);
        employeeRepository.flush();
        return EmployeeMapper.toResponse(employee);
    }

    public List<EmployeeResponseDTO> deleteAllEmployees() {
        List<EmployeeResponseDTO> employees = getAllEmployees();
        employeeRepository.deleteAll();
        return employees;
    }
}
