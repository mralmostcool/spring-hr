package mrcool.hr.entity.employee;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    List<Employee> findAllByIsActiveTrue();

    Optional<Employee> findByIdAndIsActiveTrue(UUID id);

    boolean existsByDesignationId(UUID designationId);

}

