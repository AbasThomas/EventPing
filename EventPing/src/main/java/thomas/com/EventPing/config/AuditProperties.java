package thomas.com.EventPing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "eventping.audit")
public class AuditProperties {

    private boolean enabled = true;
    private boolean logAuthentication = true;
    private boolean logAuthorizationFailures = true;
    private boolean logDataModifications = true;
    private boolean logSecurityViolations = true;
    private int retentionDays = 90;
}