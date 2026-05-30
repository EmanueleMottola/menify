package com.xazy.menify.domain

sealed class CreateUserResult {
    data class Success(val user: User) : CreateUserResult()
    data object DuplicatedUsernameFailure : CreateUserResult()
    data object DuplicatedEmailFailure : CreateUserResult()
}