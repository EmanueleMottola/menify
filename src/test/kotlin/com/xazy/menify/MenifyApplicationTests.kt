package com.xazy.menify

import com.xazy.menify.infrastructure.postgres.TestcontainerConfiguration
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@Import(TestcontainerConfiguration::class)
@SpringBootTest
class MenifyApplicationTests {

	@Test
	fun contextLoads() {
	}

}
