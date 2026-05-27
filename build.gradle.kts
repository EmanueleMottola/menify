import nu.studer.gradle.jooq.JooqGenerate
import org.flywaydb.core.Flyway
import org.testcontainers.containers.PostgreSQLContainer

plugins {
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.21"
	id("org.springframework.boot") version "4.0.6"
	id("io.spring.dependency-management") version "1.1.7"
	id("nu.studer.jooq") version "10.2"
	id("org.flywaydb.flyway") version "10.2.0"
}

group = "com.xazy"
version = "0.0.1-SNAPSHOT"

extra["jooq.version"] = "3.19.28"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-jooq")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.postgresql:postgresql:42.7.2")
	implementation("org.flywaydb:flyway-core:11.8.0")
	implementation("org.flywaydb:flyway-database-postgresql:11.8.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("io.mockk:mockk:1.14.0")
	testImplementation("com.ninja-squad:springmockk:4.0.2")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:postgresql:1.21.4")
	testImplementation("org.flywaydb:flyway-core:11.8.0")

	// jOOQ code generation dependencies
	jooqGenerator("org.jooq:jooq-codegen:3.19.28")
	jooqGenerator("org.jooq:jooq-meta:3.19.28")
	jooqGenerator("org.postgresql:postgresql:42.7.2")
	jooqGenerator("org.testcontainers:postgresql:1.21.4")
	jooqGenerator("org.slf4j:slf4j-simple:2.0.17")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

buildscript {
	dependencies {
		classpath("org.testcontainers:postgresql:1.21.0")
		classpath("org.postgresql:postgresql:42.7.5")
		classpath("org.flywaydb:flyway-database-postgresql:11.8.0")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

jooq {
	configurations {
		create("main") {
			jooqConfiguration.apply {
				generator.database.inputSchema = "public"
			}
		}
	}
}

tasks.named<JooqGenerate>("generateJooq") {
	doFirst {
		val dbContainer = PostgreSQLContainer("postgres:17").apply {
			start()
		}
		project.extra["dbContainer"] = dbContainer

		Flyway.configure()
			.locations("filesystem:$projectDir/src/main/resources/db/migration")
			.dataSource(dbContainer.jdbcUrl, dbContainer.username, dbContainer.password)
			.load()
			.migrate()

		jooq {
			configurations {
				getByName("main") {
					jooqConfiguration.jdbc.apply {
						url = dbContainer.jdbcUrl
						username = dbContainer.username
						password = dbContainer.password
					}
				}
			}
		}
	}

	inputs.files(fileTree("src/main/resources/db/migration"))
	allInputsDeclared = true

	finalizedBy("stopDbContainer")
}

tasks.register("stopDbContainer") {
	doLast {
		if (project.extra.has("dbContainer")) {
			val dbContainer: PostgreSQLContainer<*> by project.extra
			dbContainer.stop()
		}
	}
}

sourceSets.main {
	java.srcDir(layout.buildDirectory.dir("generated-src/jooq/main"))
}
