package com.cashtransfer.main;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("h2") // Add this line
class MainApplicationTests {

	@Test
	void contextLoads() {
	}

}
