package thomas.com.EventPing.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Database security configuration with connection pooling and SSL
 * **Validates: Requirements 7.1, 7.3, 4.5**
 */
@Configuration
@Slf4j
public class DatabaseSecurityConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${eventping.database.ssl.enabled:true}")
    private boolean sslEnabled;

    @Value("${eventping.database.ssl.mode:require}")
    private String sslMode;

    @Value("${eventping.database.connection-pool.maximum-pool-size:20}")
    private int maximumPoolSize;

    @Value("${eventping.database.connection-pool.minimum-idle:5}")
    private int minimumIdle;

    @Value("${eventping.database.connection-pool.connection-timeout:30000}")
    private long connectionTimeout;

    @Value("${eventping.database.connection-pool.idle-timeout:600000}")
    private long idleTimeout;

    @Value("${eventping.database.connection-pool.max-lifetime:1800000}")
    private long maxLifetime;

    @Value("${eventping.database.connection-pool.leak-detection-threshold:60000}")
    private long leakDetectionThreshold;

    @Bean
    @Primary
    public DataSource secureDataSource() {
        HikariConfig config = new HikariConfig();
        
        // Basic connection settings
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // Connection pool settings for security and performance
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setLeakDetectionThreshold(leakDetectionThreshold);
        
        // Security settings
        config.setAutoCommit(false); // Explicit transaction control
        config.setIsolateInternalQueries(true);
        config.setAllowPoolSuspension(false);
        
        // Connection validation
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);
        
        // SSL and security properties
        Properties dataSourceProperties = new Properties();
        
        if (sslEnabled) {
            dataSourceProperties.setProperty("ssl", "true");
            dataSourceProperties.setProperty("sslmode", sslMode);
            
            // Additional SSL security properties
            dataSourceProperties.setProperty("sslfactory", "org.postgresql.ssl.DefaultJavaSSLFactory");
            dataSourceProperties.setProperty("sslhostnameverifier", "org.postgresql.ssl.PGjdbcHostnameVerifier");
            
            log.info("Database SSL enabled with mode: {}", sslMode);
        } else {
            log.warn("Database SSL is disabled - this is not recommended for production");
        }
        
        // Security-related connection properties
        dataSourceProperties.setProperty("tcpKeepAlive", "true");
        dataSourceProperties.setProperty("loginTimeout", "30");
        dataSourceProperties.setProperty("connectTimeout", "30");
        dataSourceProperties.setProperty("socketTimeout", "60");
        
        // Prevent SQL injection through connection properties
        dataSourceProperties.setProperty("allowMultiQueries", "false");
        dataSourceProperties.setProperty("allowLoadLocalInfile", "false");
        dataSourceProperties.setProperty("allowUrlInLocalInfile", "false");
        
        // Application name for monitoring
        dataSourceProperties.setProperty("ApplicationName", "EventPing-Secure");
        
        config.setDataSourceProperties(dataSourceProperties);
        
        // Pool name for monitoring
        config.setPoolName("EventPing-SecurePool");
        
        // Register JMX for monitoring
        config.setRegisterMbeans(true);
        
        HikariDataSource dataSource = new HikariDataSource(config);
        
        log.info("Configured secure database connection pool with {} max connections", maximumPoolSize);
        
        return dataSource;
    }

    /**
     * Configuration properties for database security
     */
    @Bean
    public DatabaseSecurityProperties databaseSecurityProperties() {
        DatabaseSecurityProperties properties = new DatabaseSecurityProperties();
        properties.setSslEnabled(sslEnabled);
        properties.setSslMode(sslMode);
        properties.setMaximumPoolSize(maximumPoolSize);
        properties.setMinimumIdle(minimumIdle);
        properties.setConnectionTimeout(connectionTimeout);
        properties.setIdleTimeout(idleTimeout);
        properties.setMaxLifetime(maxLifetime);
        properties.setLeakDetectionThreshold(leakDetectionThreshold);
        return properties;
    }

    /**
     * Properties class for database security configuration
     */
    public static class DatabaseSecurityProperties {
        private boolean sslEnabled;
        private String sslMode;
        private int maximumPoolSize;
        private int minimumIdle;
        private long connectionTimeout;
        private long idleTimeout;
        private long maxLifetime;
        private long leakDetectionThreshold;

        // Getters and setters
        public boolean isSslEnabled() { return sslEnabled; }
        public void setSslEnabled(boolean sslEnabled) { this.sslEnabled = sslEnabled; }
        public String getSslMode() { return sslMode; }
        public void setSslMode(String sslMode) { this.sslMode = sslMode; }
        public int getMaximumPoolSize() { return maximumPoolSize; }
        public void setMaximumPoolSize(int maximumPoolSize) { this.maximumPoolSize = maximumPoolSize; }
        public int getMinimumIdle() { return minimumIdle; }
        public void setMinimumIdle(int minimumIdle) { this.minimumIdle = minimumIdle; }
        public long getConnectionTimeout() { return connectionTimeout; }
        public void setConnectionTimeout(long connectionTimeout) { this.connectionTimeout = connectionTimeout; }
        public long getIdleTimeout() { return idleTimeout; }
        public void setIdleTimeout(long idleTimeout) { this.idleTimeout = idleTimeout; }
        public long getMaxLifetime() { return maxLifetime; }
        public void setMaxLifetime(long maxLifetime) { this.maxLifetime = maxLifetime; }
        public long getLeakDetectionThreshold() { return leakDetectionThreshold; }
        public void setLeakDetectionThreshold(long leakDetectionThreshold) { this.leakDetectionThreshold = leakDetectionThreshold; }
    }
}