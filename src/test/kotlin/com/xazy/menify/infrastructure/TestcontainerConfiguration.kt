package com.xazy.menify.infrastructure

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestcontainerConfiguration {

    @Bean
    @ServiceConnection
    fun createDatabaseTestcontainer() = PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"))
}