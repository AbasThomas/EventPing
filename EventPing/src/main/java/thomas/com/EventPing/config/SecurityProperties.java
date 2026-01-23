package thomas.com.EventPing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "eventping.security")
public class SecurityProperties {

    private Jwt jwt = new Jwt();
    private Password password = new Password();
    private Session session = new Session();
    private Cors cors = new Cors();
    private Headers headers = new Headers();

    @Data
    public static class Jwt {
        private String secret;
        private long expiration = 86400000; // 24 hours
        private long refreshExpiration = 604800000; // 7 days
        private String issuer = "EventPing";
        private String audience = "EventPing-Users";
    }

    @Data
    public static class Password {
        private int bcryptRounds = 12;
        private int minLength = 8;
        private boolean requireSpecialChars = true;
    }

    @Data
    public static class Session {
        private int timeout = 1800; // 30 minutes
        private int maxConcurrent = 3;
        private boolean preventFixation = true;
    }

    @Data
    public static class Cors {
        private List<String> allowedOrigins;
        private List<String> allowedMethods;
        private List<String> allowedHeaders;
        private boolean allowCredentials = true;
        private long maxAge = 3600;
    }

    @Data
    public static class Headers {
        private String frameOptions = "DENY";
        private String contentTypeOptions = "nosniff";
        private String xssProtection = "1; mode=block";
        private Hsts hsts = new Hsts();
        private String csp;

        @Data
        public static class Hsts {
            private long maxAge = 31536000; // 1 year
            private boolean includeSubdomains = true;
        }
    }
}