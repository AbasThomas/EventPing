package thomas.com.EventPing.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import thomas.com.EventPing.plan.model.Plan;
import thomas.com.EventPing.plan.repository.PlanRepository;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder {
    private final PlanRepository planRepository;

    @PostConstruct
    public void seedDatabase() {
        seedPlans();
    }

    private void seedPlans() {
        // Seed FREE plan
        if (planRepository.findByName(Plan.PlanName.FREE).isEmpty()) {
            Plan freePlan = new Plan();
            freePlan.setName(Plan.PlanName.FREE);
            freePlan.setMaxEventsPerDay(3);
            freePlan.setMaxParticipantsPerEvent(50);
            freePlan.setReminderChannels("EMAIL");
            freePlan.setPrice(BigDecimal.ZERO);
            
            planRepository.save(freePlan);
            log.info("Seeded FREE plan");
        }

        // Seed PRO plan
        if (planRepository.findByName(Plan.PlanName.PRO).isEmpty()) {
            Plan proPlan = new Plan();
            proPlan.setName(Plan.PlanName.PRO);
            proPlan.setMaxEventsPerDay(999);
            proPlan.setMaxParticipantsPerEvent(500);
            proPlan.setReminderChannels("EMAIL,WHATSAPP");
            proPlan.setPrice(new BigDecimal("9.99"));
            
            planRepository.save(proPlan);
            log.info("Seeded PRO plan");
        }

        log.info("Database seeding completed");
    }
}
