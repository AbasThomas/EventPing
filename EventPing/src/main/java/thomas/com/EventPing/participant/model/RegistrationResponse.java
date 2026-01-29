package thomas.com.EventPing.participant.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import thomas.com.EventPing.event.model.EventCustomField;
import thomas.com.EventPing.security.validation.NoSqlInjection;
import thomas.com.EventPing.security.validation.NoXss;

import java.time.LocalDateTime;

@Entity
@Table(name = "registration_responses")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"participant", "customField"})
public class RegistrationResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_field_id", nullable = false)
    private EventCustomField customField;

    @Column(name = "response_value", columnDefinition = "TEXT")
    @Size(max = 5000, message = "Response value must not exceed 5000 characters")
    @NoXss
    @NoSqlInjection
    private String responseValue;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
