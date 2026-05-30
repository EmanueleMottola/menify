package com.xazy.menify.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import com.xazy.menify.infrastructure.postgres.TestcontainerConfiguration
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Import(TestcontainerConfiguration::class)

@SpringBootTest
@AutoConfigureMockMvc
class UsersControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val objectMapper: ObjectMapper = ObjectMapper()

    @Test
    fun `create user returns 201 with location header`() {
        val body = mapOf(
            "email" to "john@example.com",
            "username" to "johndoe",
            "password" to "securepassword123"
        )

        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        )
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", matchesPattern("/users/.*")))
    }

    @Test
    fun `create user returns 409 when email is duplicated`() {
        val body = mapOf(
            "email" to "duplicate@example.com",
            "username" to "user1",
            "password" to "password123"
        )

        // First request — should succeed
        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        ).andExpect(status().isCreated)

        // Second request with same email — should conflict
        val duplicateEmail = mapOf(
            "email" to "john@example.com",
            "username" to "user2",
            "password" to "securepassword123"
        )
        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateEmail))
        ).andExpect(status().isConflict)
    }

    @Test
    fun `create user returns 409 when username is duplicated`() {
        val body = mapOf(
            "email" to "unique@example.com",
            "username" to "takenusername",
            "password" to "password123"
        )

        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        ).andExpect(status().isCreated)

        val duplicateUsername = mapOf(
            "email" to "another@example.com",
            "username" to "takenusername",
            "password" to "password123"
        )
        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateUsername))
        ).andExpect(status().isConflict)
    }

    // --- GET /users ---

    @Test
    fun `list users returns 200 with a list`() {
        mockMvc.perform(get("/users"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
    }

    @Test
    fun `created user appears in list`() {
        val body = mapOf(
            "email" to "listed@example.com",
            "username" to "listeduser",
            "password" to "password123"
        )

        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        ).andExpect(status().isCreated)

        mockMvc.perform(get("/users"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[?(@.username == 'listeduser')]").exists())
    }
}