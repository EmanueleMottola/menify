package com.xazy.menify.usecases

import com.xazy.menify.domain.User
import com.xazy.menify.domain.UsersRepository
import org.springframework.stereotype.Service

@Service
class GetUsersUseCase(
    val usersRepository: UsersRepository
) {
    operator fun invoke(): List<User> {
        return usersRepository.getUsers()
    }
}