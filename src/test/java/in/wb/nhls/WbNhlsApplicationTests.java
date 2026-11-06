package in.wb.nhls;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

// খালি DB-তে Flyway V1 চলে, তারপর Hibernate validate পাস করলে context চালু হয়
@SpringBootTest(properties = "nhls.notification.dispatcher.enabled=false")
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class WbNhlsApplicationTests {

	@Test
	void contextLoads() {
	}

}
