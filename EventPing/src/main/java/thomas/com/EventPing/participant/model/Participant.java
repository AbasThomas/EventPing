package thomas.com.EventPing.participant.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import thomas.com.EventPing.event.model.Event;
import thomas.com.EventPing.security.validation.NoSqlInjection;
import thomas.com.EventPing.security.validation.NoXss;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "participants")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"event", "reminders"})
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @NoXss
    @NoSqlInjection
    private String email;

    @Column(name = "phone_number")
    @NoXss
    @NoSqlInjection
    private String phoneNumber;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean unsubscribed = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "rsvp_status", nullable = false)
    private RsvpStatus rsvpStatus = RsvpStatus.TENTATIVE;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<thomas.com.EventPing.reminder.model.Reminder> reminders = new ArrayList<>();

    public enum RsvpStatus {
        ATTENDING,
        NOT_ATTENDING,
        TENTATIVE
    }
}
