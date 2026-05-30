package com.xazy.menify.infrastructure.security

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class JwtTestConfiguration {
    @Bean
    fun jwtService(): JwtService = JwtService("test-secret-key-must-be-at-least-32-chars")
}