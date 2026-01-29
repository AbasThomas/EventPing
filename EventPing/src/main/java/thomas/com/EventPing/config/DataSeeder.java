package thomas.com.EventPing.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import thomas.com.EventPing.plan.repository.PlanRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder {
    private final PlanRepository planRepository;

    @PostConstruct
    @Transactional
    public void seedDatabase() {
        if (planRepository.count() == 0) {
            log.info("No plans found. Seeding initial plans...");
            
            // Create FREE Plan
            thomas.com.EventPing.plan.model.Plan freePlan = new thomas.com.EventPing.plan.model.Plan();
            freePlan.setName(thomas.com.EventPing.plan.model.Plan.PlanName.FREE);
            freePlan.setMaxEventsPerDay(3);
            freePlan.setMaxParticipantsPerEvent(20);
            freePlan.setReminderChannels("EMAIL");
            freePlan.setMonthlyCreditLimit(50);
            planRepository.save(freePlan);
            
            // Create BASIC Plan
            thomas.com.EventPing.plan.model.Plan basicPlan = new thomas.com.EventPing.plan.model.Plan();
            basicPlan.setName(thomas.com.EventPing.plan.model.Plan.PlanName.BASIC);
            basicPlan.setMaxEventsPerDay(10);
            basicPlan.setMaxParticipantsPerEvent(100);
            basicPlan.setReminderChannels("EMAIL,TELEGRAM");
            basicPlan.setMonthlyCreditLimit(500);
            planRepository.save(basicPlan);
            
            // Create PRO Plan
            thomas.com.EventPing.plan.model.Plan proPlan = new thomas.com.EventPing.plan.model.Plan();
            proPlan.setName(thomas.com.EventPing.plan.model.Plan.PlanName.PRO);
            proPlan.setMaxEventsPerDay(50);
            proPlan.setMaxParticipantsPerEvent(500);
            proPlan.setReminderChannels("EMAIL,TELEGRAM,WHATSAPP");
            proPlan.setMonthlyCreditLimit(5000);
            planRepository.save(proPlan);
            
            log.info("Initial plans seeded successfully.");
        } else {
            log.info("Database already seeded with {} plans", planRepository.count());
        }
    }
}
