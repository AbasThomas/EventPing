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

    @Column(name = "max_events_per_day") // Null means unlimited
    private Integer maxEventsPerDay;

    @Column(name = "max_participants_per_event") // Null means unlimited
    private Integer maxParticipantsPerEvent;

    @Column(name = "reminder_channels", nullable = false)
    private String reminderChannels; // Comma-separated: EMAIL,WHATSAPP,TELEGRAM,DISCORD

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "is_enterprise", nullable = false)
    private boolean enterprise = false;

    @Column(name = "has_custom_intervals", nullable = false)
    private boolean customIntervalsEnabled = false;

    @Column(name = "has_analytics", nullable = false)
    private boolean analyticsEnabled = false;

    @Column(name = "has_custom_branding", nullable = false)
    private boolean customBrandingEnabled = false;

    @Column(name = "has_custom_templates", nullable = false)
    private boolean customTemplatesEnabled = false;

    @Column(name = "has_advanced_rsvp", nullable = false)
    private boolean advancedRsvpEnabled = false;

    @Column(name = "has_api_access", nullable = false)
    private boolean apiAccessEnabled = false;

    @Column(name = "max_team_members", nullable = false)
    private Integer maxTeamMembers = 0;

    @Column(name = "monthly_credit_limit") // Null means unlimited
    private Integer monthlyCreditLimit;

    public enum PlanName {
        FREE,
        BASIC,
        PRO,
        BUSINESS,
        ENTERPRISE
    }
}
