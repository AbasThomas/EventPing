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
            
            savePlan(Plan.PlanName.FREE, 3, 20, "EMAIL", false, 100);
            savePlan(Plan.PlanName.BASIC, 10, 100, "EMAIL,TELEGRAM", false, 1000);
            savePlan(Plan.PlanName.PRO, 50, 500, "EMAIL,TELEGRAM,WHATSAPP", false, 5000);
            savePlan(Plan.PlanName.BUSINESS, null, 2000, "EMAIL,TELEGRAM,WHATSAPP,DISCORD", false, 25000);
            savePlan(Plan.PlanName.ENTERPRISE, null, null, "EMAIL,TELEGRAM,WHATSAPP,DISCORD", true, null);
            
            log.info("Initial plans seeded successfully.");
        }
    }

    private void savePlan(Plan.PlanName name, Integer maxEvents, Integer maxParticipants, String channels, boolean enterprise, Integer credits) {
        Plan plan = new Plan();
        plan.setName(name);
        plan.setMaxEventsPerDay(maxEvents);
        plan.setMaxParticipantsPerEvent(maxParticipants);
        plan.setReminderChannels(channels);
        plan.setEnterprise(enterprise);
        plan.setMonthlyCreditLimit(credits);
        planRepository.save(plan);
    }
}
