package mrcool.hr.designation;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignationRepository extends JpaRepository<Designation, UUID> {

    boolean existsByName(String name);

}
