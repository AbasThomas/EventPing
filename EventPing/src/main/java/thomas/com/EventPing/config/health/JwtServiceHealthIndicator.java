package thomas.com.EventPing.config.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import thomas.com.EventPing.config.SecurityProperties;

@Component
@RequiredArgsConstructor
public class JwtServiceHealthIndicator implements HealthIndicator {

    private final SecurityProperties securityProperties;

    @Override
    public Health health() {
        try {
            if (securityProperties.getJwt() == null) {
                return Health.down().withDetail("error", "JWT configuration missing").build();
            }
            
            String secret = securityProperties.getJwt().getSecret();
            if (secret == null || secret.length() < 32) {
                return Health.down().withDetail("error", "JWT secret invalid or too short").build();
            }
            
            return Health.up()
                    .withDetail("issuer", securityProperties.getJwt().getIssuer())
                    .withDetail("expiration", securityProperties.getJwt().getExpiration())
                    .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
