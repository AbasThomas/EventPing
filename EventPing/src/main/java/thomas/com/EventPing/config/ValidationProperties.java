package thomas.com.EventPing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "eventping.validation")
public class ValidationProperties {

    private SqlInjection sqlInjection = new SqlInjection();
    private Xss xss = new Xss();
    private Email email = new Email();
    private FileUpload fileUpload = new FileUpload();

    @Data
    public static class SqlInjection {
        private boolean enabled = true;
    }

    @Data
    public static class Xss {
        private boolean enabled = true;
    }

    @Data
    public static class Email {
        private boolean domainValidation = true;
    }

    @Data
    public static class FileUpload {
        private DataSize maxSize = DataSize.ofMegabytes(10);
        private List<String> allowedTypes;
    }
}