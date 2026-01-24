package thomas.com.EventPing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "eventping.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;
    private Redis redis = new Redis();
    private Global global = new Global();
    private Ip ip = new Ip();
    private User user = new User();
    private Endpoint endpoint = new Endpoint();
    private Plan plan = new Plan();
    private Registration registration = new Registration();
    private Login login = new Login();
    private EventCreation eventCreation = new EventCreation();
    private ParticipantJoin participantJoin = new ParticipantJoin();
    private Free free = new Free();
    private Pro pro = new Pro();

    @Data
    public static class Redis {
        private boolean enabled = false;
    }

    @Data
    public static class Global {
        private int requestsPerSecond = 100;
        private int requestsPerMinute = 1000;
        private int requestsPerHour = 10000;
    }

    @Data
    public static class Ip {
        private int requestsPerMinute = 100;
        private int requestsPerHour = 1000;
        private int blockDuration = 300; // seconds
    }

    @Data
    public static class User {
        private int apiRequestsPerMinute = 60;
        private int apiRequestsPerHour = 1000;
    }

    @Data
    public static class Endpoint {
        private int requestsPerMinute = 200;
        private int requestsPerHour = 2000;
    }

    @Data
    public static class Plan {
        private int basicRequestsPerHour = 500;
        private int premiumRequestsPerHour = 2000;
        private int enterpriseRequestsPerHour = 10000;
    }

    @Data
    public static class Registration {
        private int attemptsPerHour = 5;
    }

    @Data
    public static class Login {
        private int attemptsPerHour = 10;
    }

    @Data
    public static class EventCreation {
        private int perHour = 10;
    }

    @Data
    public static class ParticipantJoin {
        private int perMinute = 5;
    }

    @Data
    public static class Free {
        private int maxEventsPerDay = 3;
        private int maxParticipantsPerEvent = 20;
        private int maxRemindersPerDay = 50;
    }

    @Data
    public static class Pro {
        private int maxEventsPerDay = 100;
        private int maxParticipantsPerEvent = 500;
        private int maxRemindersPerDay = 5000;
    }
}