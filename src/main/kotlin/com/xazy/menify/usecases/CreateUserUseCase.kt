package com.xazy.menify.usecases

import com.xazy.menify.domain.CreateUserCommand
import com.xazy.menify.domain.UsersRepository
import com.xazy.menify.domain.CreateUserResult
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class CreateUserUseCase(
    val passwordEncoder: PasswordEncoder,
    val usersRepository: UsersRepository
) {
    operator fun invoke(command: CreateUserCommand): CreateUserResult {
        val hashedPassword =
            passwordEncoder.encode(command.password) ?: throw IllegalStateException("Password encoding failed")
        val hashedCommand = command.copy(password = hashedPassword)
        return usersRepository.create(hashedCommand)
    }
}

