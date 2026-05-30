package com.xazy.menify.usecases

import com.xazy.menify.domain.CreateUserCommand
import com.xazy.menify.domain.CreateUserResult
import com.xazy.menify.domain.UsersRepository
import com.xazy.menify.generators.createTestUser
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateUserUseCaseTest {
    private val usersRepository: UsersRepository = mockk()
    private val useCase: CreateUserUseCase = CreateUserUseCase(usersRepository)

    @Test
    fun `should return success if create user successfully`() {
        val user = createTestUser()
        val command = createUserCommand
        every { usersRepository.create(command) } returns CreateUserResult.Success(user)

        val result = useCase(command)

        assertEquals(CreateUserResult.Success(user), result)
    }

    @Test
    fun `should return DuplicatedEmailFailure if create user fails due to duplicate email`() {
        val command = createUserCommand
        every { usersRepository.create(command) } returns CreateUserResult.DuplicatedEmailFailure

        val result = useCase(command)

        assertTrue(result is CreateUserResult.DuplicatedEmailFailure)
    }

    @Test
    fun `should return DuplicatedUsernameFailure if create user fails due to duplicate usernameg`() {
        val command = createUserCommand
        every { usersRepository.create(command) } returns CreateUserResult.DuplicatedUsernameFailure

        val result = useCase(command)

        assertTrue(result is CreateUserResult.DuplicatedUsernameFailure)
    }

    companion object {
        const val USERNAME: String = "username"
        const val PASSWORD: String = "password"
        const val EMAIL: String = "email"

        private val createUserCommand = CreateUserCommand(
            email = EMAIL,
            password = PASSWORD,
            username = USERNAME
        )
    }
}