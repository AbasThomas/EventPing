package thomas.com.EventPing.plan.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private PlanName name;

    @Column(name = "max_events_per_day", nullable = false)
    private Integer maxEventsPerDay;

    @Column(name = "max_participants_per_event", nullable = false)
    private Integer maxParticipantsPerEvent;

    @Column(name = "reminder_channels", nullable = false)
    private String reminderChannels; // Comma-separated: EMAIL,WHATSAPP

    @Column(nullable = false)
    private BigDecimal price;

    public enum PlanName {
        FREE,
        PRO
    }
}
