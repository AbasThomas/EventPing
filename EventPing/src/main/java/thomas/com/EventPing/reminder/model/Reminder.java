package thomas.com.EventPing.reminder.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import thomas.com.EventPing.event.model.Event;
import thomas.com.EventPing.participant.model.Participant;

import java.time.LocalDateTime;

@Entity
@Table(name = "reminders")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"event", "participant"})
public class Reminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @Column(name = "send_at", nullable = false)
    private LocalDateTime sendAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReminderChannel channel = ReminderChannel.EMAIL;

    @Column(nullable = false)
    private Boolean sent = false;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    public enum ReminderChannel {
        EMAIL,
        WHATSAPP,
        TELEGRAM,
        DISCORD
    }
}
