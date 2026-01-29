package thomas.com.EventPing.event.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_integrations")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "event")
public class EventIntegration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(name = "integration_type", nullable = false)
    @NotNull(message = "Integration type is required")
    private IntegrationType integrationType;

    @Column(name = "configuration", columnDefinition = "JSONB")
    private String configuration; // JSON string for future OAuth tokens, webhooks

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum IntegrationType {
        EMAIL,
        WHATSAPP,
        TELEGRAM,
        DISCORD,
        SLACK,
        SMS
    }
}
