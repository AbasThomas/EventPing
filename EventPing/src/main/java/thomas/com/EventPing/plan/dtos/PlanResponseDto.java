package thomas.com.EventPing.plan.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import thomas.com.EventPing.plan.model.Plan;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanResponseDto {
    private Long id;
    private Plan.PlanName name;
    private Integer maxEventsPerDay;
    private Integer maxParticipantsPerEvent;
    private String reminderChannels;
    private Integer monthlyCreditLimit;
    private boolean enterprise;
}
