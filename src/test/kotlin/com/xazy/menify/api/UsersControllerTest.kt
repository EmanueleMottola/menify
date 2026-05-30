package com.xazy.menify.api

import com.ninjasquad.springmockk.MockkBean
import com.xazy.menify.domain.CreateUserResult
import com.xazy.menify.generators.createTestUser
import com.xazy.menify.usecases.CreateUserUseCase
import com.xazy.menify.usecases.GetUsersUseCase
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(UsersController::class)
class UsersControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var createUsers: CreateUserUseCase

    @MockkBean
    lateinit var getUsersUseCase: GetUsersUseCase

    @Test
    fun `create - creates a user`() {
        every { createUsers(any()) } returns CreateUserResult.Success(createTestUser(userId = "123"))

        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email": "john@email.com", "username": "john", "password": "pass"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", "/users/123"))
    }

    @Test
    fun `create - does not create a user due to duplicated email`() {
        every { createUsers(any()) } returns CreateUserResult.DuplicatedEmailFailure

        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email": "john@email.com", "username": "john", "password": "pass"}""")
        )
            .andExpect(status().isConflict)
    }

    @Test
    fun `create - does not create a user due to duplicated username`() {
        every { createUsers(any()) } returns CreateUserResult.DuplicatedUsernameFailure

        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email": "john@email.com", "username": "john", "password": "pass"}""")
        )
            .andExpect(status().isConflict)
    }
}