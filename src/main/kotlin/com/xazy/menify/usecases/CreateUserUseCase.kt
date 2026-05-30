package com.xazy.menify.usecases

import com.xazy.menify.domain.CreateUserCommand
import com.xazy.menify.domain.UsersRepository
import com.xazy.menify.domain.CreateUserResult
import org.springframework.stereotype.Component

@Component
class CreateUserUseCase(
    val usersRepository: UsersRepository
) {
    operator fun invoke(command: CreateUserCommand): CreateUserResult = usersRepository.create(command)
}

