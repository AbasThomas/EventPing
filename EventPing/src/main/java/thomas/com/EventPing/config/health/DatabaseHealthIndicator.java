package thomas.com.EventPing.config.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component("customDatabaseHealthIndicator")
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1000)) {
                return Health.up()
                        .withDetail("database", connection.getMetaData().getDatabaseProductName())
                        .withDetail("version", connection.getMetaData().getDatabaseProductVersion())
                        .build();
            } else {
                return Health.down().withDetail("error", "Connection invalid").build();
            }
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
