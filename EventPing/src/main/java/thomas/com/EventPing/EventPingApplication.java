package thomas.com.EventPing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EventPingApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventPingApplication.class, args);
	}

}
