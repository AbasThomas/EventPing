package thomas.com.EventPing;

import org.springframework.boot.SpringApplication;

public class TestEventPingApplication {

	public static void main(String[] args) {
		SpringApplication.from(EventPingApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
