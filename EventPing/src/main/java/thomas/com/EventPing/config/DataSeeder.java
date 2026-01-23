package thomas.com.EventPing.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import thomas.com.EventPing.plan.repository.PlanRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder {
    private final PlanRepository planRepository;

    @PostConstruct
    public void seedDatabase() {
        // Plans are now seeded via Flyway migration V1__Initial_Schema.sql
        // This seeder is kept for reference but won't insert duplicates
        long planCount = planRepository.count();
        if (planCount == 0) {
            log.warn("No plans found in database - Flyway migration may not have run!");
        } else {
            log.info("Database already seeded with {} plans via Flyway", planCount);
        }
    }
}
