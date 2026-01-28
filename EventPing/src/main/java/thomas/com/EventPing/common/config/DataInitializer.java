package thomas.com.EventPing.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import thomas.com.EventPing.plan.model.Plan;
import thomas.com.EventPing.plan.repository.PlanRepository;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PlanRepository planRepository;

    @Override
    public void run(String... args) {
        seedPlans();
    }

    private void seedPlans() {
        if (planRepository.count() == 0) {
            log.info("Seeding initial plans...");
            
            Plan freePlan = new Plan();
            freePlan.setName(Plan.PlanName.FREE);
            freePlan.setMaxEventsPerDay(3);
            freePlan.setMaxParticipantsPerEvent(50);
            freePlan.setReminderChannels("EMAIL");
            freePlan.setPrice(BigDecimal.ZERO);
            planRepository.save(freePlan);

            Plan proPlan = new Plan();
            proPlan.setName(Plan.PlanName.PRO);
            proPlan.setMaxEventsPerDay(999);
            proPlan.setMaxParticipantsPerEvent(500);
            proPlan.setReminderChannels("EMAIL,WHATSAPP");
            proPlan.setPrice(new BigDecimal("9.99"));
            planRepository.save(proPlan);
            
            log.info("Initial plans seeded successfully.");
        }
    }
}
