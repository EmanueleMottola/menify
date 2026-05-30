package com.xazy.menify.domain

data class CreateUserCommand(val email: String, val username: String, val password: String)