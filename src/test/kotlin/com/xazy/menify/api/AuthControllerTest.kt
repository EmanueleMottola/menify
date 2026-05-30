package com.xazy.menify.api

import com.ninjasquad.springmockk.MockkBean
import com.xazy.menify.api.auth.AuthController
import com.xazy.menify.domain.CreateUserResult
import com.xazy.menify.generators.createTestUser
import com.xazy.menify.infrastructure.security.JwtService
import com.xazy.menify.usecases.CreateUserUseCase
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AuthController::class, excludeAutoConfiguration = [SecurityAutoConfiguration::class])
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var createUser: CreateUserUseCase

    @MockkBean
    lateinit var jwtService: JwtService

    @Test
    fun `signup - creates a user and returns a token`() {
        every { createUser(any()) } returns CreateUserResult.Success(createTestUser(userId = "123"))
        every { jwtService.generate("123") } returns "mocked.jwt.token"

        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email": "john@email.com", "username": "john", "password": "password123"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.accessToken").value("mocked.jwt.token"))
    }

    @Test
    fun `signup - returns 409 when email is duplicated`() {
        every { createUser(any()) } returns CreateUserResult.DuplicatedEmailFailure

        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email": "john@email.com", "username": "john", "password": "password123"}""")
        )
            .andExpect(status().isConflict)
    }

    @Test
    fun `signup - returns 409 when username is duplicated`() {
        every { createUser(any()) } returns CreateUserResult.DuplicatedUsernameFailure

        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email": "john@email.com", "username": "john", "password": "password123"}""")
        )
            .andExpect(status().isConflict)
    }

    @Test
    fun `signup - returns 400 when request body is invalid`() {
        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email": "not-an-email", "username": "j", "password": "short"}""")
        )
            .andExpect(status().isBadRequest)
    }
}