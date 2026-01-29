package thomas.com.EventPing.event.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import thomas.com.EventPing.security.validation.NoSqlInjection;
import thomas.com.EventPing.security.validation.NoXss;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_custom_fields")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "event")
public class EventCustomField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "field_name", nullable = false)
    @NotBlank(message = "Field name is required")
    @Size(max = 255, message = "Field name must not exceed 255 characters")
    @NoXss
    @NoSqlInjection
    private String fieldName;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false)
    @NotNull(message = "Field type is required")
    private FieldType fieldType;

    @Column(name = "is_required", nullable = false)
    private boolean required = false;

    @Column(name = "placeholder_text")
    @Size(max = 255, message = "Placeholder text must not exceed 255 characters")
    @NoXss
    @NoSqlInjection
    private String placeholderText;

    @Column(name = "field_options", columnDefinition = "TEXT")
    @NoXss
    @NoSqlInjection
    private String fieldOptions; // Comma-separated for SELECT type

    @Column(name = "display_order", nullable = false)
    @Min(value = 0, message = "Display order must be non-negative")
    private int displayOrder = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum FieldType {
        TEXT,
        EMAIL,
        PHONE,
        SELECT,
        CHECKBOX
    }
}
