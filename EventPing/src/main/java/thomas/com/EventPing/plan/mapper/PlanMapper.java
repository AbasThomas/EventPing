package thomas.com.EventPing.plan.mapper;

import org.mapstruct.Mapper;
import thomas.com.EventPing.plan.dtos.PlanResponseDto;
import thomas.com.EventPing.plan.model.Plan;

@Mapper(componentModel = "spring")
public interface PlanMapper {
    PlanResponseDto toPlanResponseDto(Plan plan);
}
