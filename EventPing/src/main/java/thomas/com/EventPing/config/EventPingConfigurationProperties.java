package thomas.com.EventPing.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    SecurityProperties.class,
    RateLimitProperties.class,
    ValidationProperties.class,
    AuditProperties.class
})
public class EventPingConfigurationProperties {
    // This class enables all custom configuration properties
}