package com.xazy.menify.api.auth

import com.xazy.menify.api.auth.dto.CreateUserBody
import com.xazy.menify.api.auth.dto.SignUpResponse
import com.xazy.menify.domain.CreateUserCommand
import com.xazy.menify.domain.CreateUserResult
import com.xazy.menify.infrastructure.security.JwtService
import com.xazy.menify.usecases.CreateUserUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val createUser: CreateUserUseCase,
    private val jwtService: JwtService
) {
    @PostMapping("/signup")
    fun signUp(@Valid @RequestBody createUserBody: CreateUserBody): ResponseEntity<SignUpResponse> {
        val command = CreateUserCommand(
            email = createUserBody.email,
            username = createUserBody.username,
            password = createUserBody.password
        )
        return when (val result = createUser(command)) {
            is CreateUserResult.Success ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(SignUpResponse(jwtService.generate(result.user.id)))
            is CreateUserResult.DuplicatedEmailFailure ->
                ResponseEntity.status(HttpStatus.CONFLICT).build()
            is CreateUserResult.DuplicatedUsernameFailure ->
                ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
    }
}