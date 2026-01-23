package thomas.com.EventPing.plan.service;

import thomas.com.EventPing.plan.dtos.PlanResponseDto;
import thomas.com.EventPing.plan.model.Plan;

public interface PlanService {
    PlanResponseDto getPlanByName(Plan.PlanName name);
}
