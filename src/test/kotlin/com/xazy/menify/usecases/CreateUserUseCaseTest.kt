package com.xazy.menify.usecases

import com.xazy.menify.domain.CreateUserCommand
import com.xazy.menify.domain.CreateUserResult
import com.xazy.menify.domain.UsersRepository
import com.xazy.menify.generators.createTestUser
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CreateUserUseCaseTest {
    private val usersRepository: UsersRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()
    private val useCase: CreateUserUseCase = CreateUserUseCase(passwordEncoder, usersRepository)

    @Test
    fun `should return success if create user successfully`() {
        val user = createTestUser()
        val command = createUserCommand
        every { passwordEncoder.encode(command.password) } returns "hashedPassword"
        every { usersRepository.create(command.copy(password = "hashedPassword")) } returns CreateUserResult.Success(user)

        val result = useCase(command)

        verify { usersRepository.create(command.copy(password = "hashedPassword")) }
        assertEquals(CreateUserResult.Success(user), result)
    }

    @Test
    fun `should return DuplicatedEmailFailure if create user fails due to duplicate email`() {
        val command = createUserCommand
        every { passwordEncoder.encode(command.password) } returns "hashedPassword"
        every { usersRepository.create(command.copy(password = "hashedPassword")) } returns CreateUserResult.DuplicatedEmailFailure

        val result = useCase(command)

        verify { usersRepository.create(command.copy(password = "hashedPassword")) }
        assertEquals(CreateUserResult.DuplicatedEmailFailure, result)
    }

    @Test
    fun `should return DuplicatedUsernameFailure if create user fails due to duplicate usernameg`() {
        val command = createUserCommand
        every { passwordEncoder.encode(command.password) } returns "hashedPassword"
        every { usersRepository.create(command.copy(password = "hashedPassword")) } returns CreateUserResult.DuplicatedUsernameFailure

        val result = useCase(command)

        assertEquals(CreateUserResult.DuplicatedUsernameFailure, result)
    }

    @Test
    fun `should throw IllegalStateException if password encoding fails`() {
        val command = createUserCommand
        every { passwordEncoder.encode(command.password) } returns null

        assertFailsWith<IllegalStateException> {
            useCase(command)
        }
        verify { usersRepository wasNot Called }
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