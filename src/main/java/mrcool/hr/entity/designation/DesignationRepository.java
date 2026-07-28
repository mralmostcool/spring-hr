package mrcool.hr.entity.designation;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DesignationRepository extends JpaRepository<Designation, UUID> {

    boolean existsByName(String name);

    @Query("SELECT MAX(d.rank) FROM Designation d")
    Integer findMaxRank();

    @Query("SELECT d FROM Designation d ORDER BY d.rank ASC NULLS LAST, d.name ASC")
    java.util.List<Designation> findAllOrderByRankAsc();

}
