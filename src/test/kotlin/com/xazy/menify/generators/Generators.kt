package com.xazy.menify.generators

import com.xazy.menify.domain.User
import java.util.UUID

fun createTestUser(
    userId : String = UUID.randomUUID().toString(),
    username : String = "username"
) = User(
    id = userId,
    username = username,
)