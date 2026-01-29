package thomas.com.EventPing.event.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import thomas.com.EventPing.User.model.User;
import thomas.com.EventPing.security.validation.NoSqlInjection;
import thomas.com.EventPing.security.validation.NoXss;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"participants", "reminders"})
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Event title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @NoXss
    @NoSqlInjection
    private String title;

    @Column(columnDefinition = "TEXT")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    @NoXss
    @NoSqlInjection
    private String description;

    @Column(name = "event_date_time", nullable = false)
    @NotNull(message = "Event date and time is required")
    @Future(message = "Event date must be in the future")
    private LocalDateTime eventDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status = EventStatus.ACTIVE;

    @Column(nullable = false, unique = true)
    @Pattern(regexp = "^[a-zA-Z0-9-_]{8,50}$", message = "Invalid slug format")
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(name = "max_participants")
    @Min(value = 1, message = "Maximum participants must be at least 1")
    @Max(value = 10000, message = "Maximum participants cannot exceed 10000")
    private Integer maxParticipants;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<thomas.com.EventPing.participant.model.Participant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<thomas.com.EventPing.reminder.model.Reminder> reminders = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventCustomField> customFields = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventIntegration> integrations = new ArrayList<>();

    @Column(name = "registration_enabled", nullable = false)
    private Boolean registrationEnabled = true;

    @PrePersist
    public void prePersist() {
        if (slug == null || slug.isEmpty()) {
            slug = generateSecureSlug();
        }
    }

    private String generateSecureSlug() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public enum EventStatus {
        ACTIVE,
        EXPIRED
    }
}
