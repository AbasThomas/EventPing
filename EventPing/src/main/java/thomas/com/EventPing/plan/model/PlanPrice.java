package thomas.com.EventPing.plan.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "plan_prices")
@Getter
@Setter
@NoArgsConstructor
public class PlanPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(nullable = false)
    private String currency; // e.g. USD, EUR, GBP

    @Column(precision = 19, scale = 2) // Null means "Contact Sales" for Enterprise
    private BigDecimal amount;

    @Column(nullable = false)
    private String region; // e.g. US, EU, GLOBAL

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "billing_period", nullable = false)
    private String billingPeriod = "MONTHLY"; // MONTHLY, YEARLY
}
