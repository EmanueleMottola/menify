package com.xazy.menify.domain

/**
 * Repository to manipulate the users.
 */
interface UsersRepository {
    fun getUsers(): List<User>

    fun create(user: CreateUserCommand): CreateUserResult
}