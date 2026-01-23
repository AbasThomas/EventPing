package thomas.com.EventPing.plan.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import thomas.com.EventPing.plan.dtos.PlanResponseDto;
import thomas.com.EventPing.plan.mapper.PlanMapper;
import thomas.com.EventPing.plan.model.Plan;
import thomas.com.EventPing.plan.repository.PlanRepository;
import thomas.com.EventPing.plan.service.PlanService;

@Service
@RequiredArgsConstructor
public class PlanServiceImplementation implements PlanService {
    private final PlanRepository planRepository;
    private final PlanMapper planMapper;

    @Override
    public PlanResponseDto getPlanByName(Plan.PlanName name) {
        Plan plan = planRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Plan not found: " + name));
        return planMapper.toPlanResponseDto(plan);
    }
}
