package com.xazy.menify.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.test.context.SpringBootTest
import com.xazy.menify.infrastructure.postgres.TestcontainerConfiguration
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Import(TestcontainerConfiguration::class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private val objectMapper: ObjectMapper = ObjectMapper()

    @Test
    fun `signup - returns 201 with access token`() {
        val body = mapOf(
            "email" to "john@example.com",
            "username" to "johndoe",
            "password" to "securepassword123"
        )

        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.accessToken").isNotEmpty)
    }

    @Test
    fun `signup - returns 409 when email is duplicated`() {
        val firstUser = mapOf(
            "email" to "duplicate@example.com",
            "username" to "user1",
            "password" to "password123"
        )
        val secondUser = mapOf(
            "email" to "duplicate@example.com",
            "username" to "user2",
            "password" to "password123"
        )

        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstUser))
        ).andExpect(status().isCreated)

        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondUser))
        ).andExpect(status().isConflict)
    }

    @Test
    fun `signup - returns 409 when username is duplicated`() {
        val firstUser = mapOf(
            "email" to "unique1@example.com",
            "username" to "takenusername",
            "password" to "password123"
        )
        val secondUser = mapOf(
            "email" to "unique2@example.com",
            "username" to "takenusername",
            "password" to "password123"
        )

        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstUser))
        ).andExpect(status().isCreated)

        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondUser))
        ).andExpect(status().isConflict)
    }

    @Test
    fun `signup - returns 400 when request body is invalid`() {
        val body = mapOf(
            "email" to "not-an-email",
            "username" to "j",
            "password" to "short"
        )

        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        ).andExpect(status().isBadRequest)
    }
}