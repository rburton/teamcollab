package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findByName(String name);

    @Query("SELECT p FROM Plan p LEFT JOIN FETCH p.planDetails WHERE p.id = :id")
    Optional<Plan> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT p FROM Plan p LEFT JOIN FETCH p.planDetails")
    List<Plan> findAllWithDetails();
}
