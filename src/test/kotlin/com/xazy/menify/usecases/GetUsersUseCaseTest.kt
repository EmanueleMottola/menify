package com.xazy.menify.usecases

import com.xazy.menify.domain.User
import com.xazy.menify.domain.UsersRepository
import com.xazy.menify.generators.createTestUser
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.UUID
import kotlin.test.assertEquals

class GetUsersUseCaseTest {
    @Test
    fun `getUsers - should return a list of users`() {
        val users = listOf(createTestUser("username"))
        val usersRepositoryMock: UsersRepository = mock()

        `when`(usersRepositoryMock.getUsers()).thenReturn(users)

        val getUsersUseCase = GetUsersUseCase(usersRepositoryMock)
        val result = getUsersUseCase()

        assertEquals(users, result)
    }
}