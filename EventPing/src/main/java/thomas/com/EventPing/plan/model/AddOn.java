package thomas.com.EventPing.plan.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "add_ons")
@Getter
@Setter
@NoArgsConstructor
public class AddOn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AddOnType type;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private String currency = "USD";

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    // Optional metadata for the add-on logic (e.g. number of credits)
    @Column(name = "credit_amount")
    private Integer creditAmount;

    public enum AddOnType {
        EVENT_UPGRADE,
        MESSAGING_CREDITS,
        CUSTOM_DOMAIN,
        PREMIUM_SUPPORT
    }
}
