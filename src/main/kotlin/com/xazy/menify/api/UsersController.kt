package com.xazy.menify.api

import com.xazy.menify.api.dto.CreateUserBody
import com.xazy.menify.domain.CreateUserCommand
import com.xazy.menify.domain.CreateUserResult
import com.xazy.menify.domain.User
import com.xazy.menify.usecases.CreateUserUseCase
import com.xazy.menify.usecases.GetUsersUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI


@RestController
@RequestMapping("/users")
class UsersController(
    private val getUsersUseCase: GetUsersUseCase,
    private val createUsers: CreateUserUseCase
) {

    @GetMapping
    fun list() : List<User> {
        return getUsersUseCase()
    }

    @PostMapping
    fun create(@RequestBody user: CreateUserBody): ResponseEntity<Nothing> {
        val createCommand = CreateUserCommand(
            user.email,
            user.username,
            user.password
        )

        return when(val result = createUsers(createCommand)) {
            is CreateUserResult.Success -> ResponseEntity
                .created(URI.create("/users/" + result.user.id)).build()
            is CreateUserResult.DuplicatedEmailFailure -> ResponseEntity<Nothing>(HttpStatus.CONFLICT)
            is CreateUserResult.DuplicatedUsernameFailure -> ResponseEntity<Nothing>(HttpStatus.CONFLICT)
        }
    }
}

