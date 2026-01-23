package thomas.com.EventPing.plan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import thomas.com.EventPing.plan.model.Plan;

import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findByName(Plan.PlanName name);
}
