package com.xazy.menify.infrastructure.postgres

import com.xazy.menify.domain.CreateUserCommand
import com.xazy.menify.domain.CreateUserResult
import com.xazy.menify.domain.User
import com.xazy.menify.domain.UsersRepository
import org.jooq.DSLContext
import org.jooq.generated.Tables
import org.jooq.generated.tables.records.UsersRecord
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals

@Import(TestcontainerConfiguration::class)
@SpringBootTest
class PostgresUsersRepositoryTest {

    @Autowired
    private lateinit var repository: UsersRepository

    @Autowired
    private lateinit var dslContext: DSLContext

    @BeforeEach
    fun setUp() {
        dslContext.deleteFrom(Tables.USERS).execute()
    }

    @Test
    fun `create - user is saved correctly`() {
        val username = "username"
        val user = CreateUserCommand(email = "random@test.com", username = username, password = "password")

        val result = repository.create(user)

        val savedUser = getSavedUser(username)
        assertTrue(savedUser?.id is Int)
        assertTrue(savedUser?.userId is UUID)
        assertEquals(user.username, savedUser?.username)
        assertEquals(user.email, savedUser?.email)
        assertEquals(user.password, savedUser?.password)
        assertEquals(CreateUserResult.Success(User(id= savedUser?.userId.toString(), username = "username")), result)
    }

    private fun getSavedUser(username: String? = null, email: String? = null): UsersRecord? {
        val selectStatement = dslContext.selectFrom(Tables.USERS)

        val conditionStatement = if (username != null && email != null) {
            selectStatement.where(Tables.USERS.USERNAME.eq(username))
                .or(Tables.USERS.EMAIL.eq(email))
        } else if (email != null) {
            selectStatement.where(Tables.USERS.EMAIL.eq(email))
        } else {
            selectStatement.where(Tables.USERS.USERNAME.eq(username))
        }

        return conditionStatement.fetchOne()
    }

    @Test
    fun `create - two users with the same username should not be saved`() {
        val username = "username"
        val email1 = "random1@test.com"
        val user1 = CreateUserCommand(email = email1, username = username, password = "password1")
        val user2 = CreateUserCommand(email = "random2@test.com", username = username, password = "password2")

        val result1 = repository.create(user1)
        assertTrue { result1 is CreateUserResult.Success}

        val result2 = repository.create(user2)
        assertTrue { result2 is CreateUserResult.DuplicatedUsernameFailure }

        val user = getSavedUser(username = username)
        assertEquals(email1, user?.email)
    }

    @Test
    fun `create - two users with the same email should not be saved`() {
        val email = "random2@test.com"
        val username1 = "username1"
        val user1 = CreateUserCommand(email = email, username = username1, password = "password2")
        val user2 = CreateUserCommand(email = email, username = "username2", password = "password2")

        val result1 = repository.create(user1)
        assertTrue { result1 is CreateUserResult.Success}

        val result2 = repository.create(user2)
        assertTrue { result2 is CreateUserResult.DuplicatedEmailFailure }

        val user = getSavedUser(email = email)
        assertEquals(username1, user?.username)
    }

    @Test
    fun `create - should handle concurrent registration gracefully`() {
        val threadCount = 2
        val latch = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(threadCount)
        val successCount = AtomicInteger(0)
        val failureCount = AtomicInteger(0)

        val command = CreateUserCommand(
            email = "race@condition.com",
            username = "racer",
            password = "password123"
        )

        for (i in 1..threadCount) {
            executor.submit {
                latch.await()

                val result = repository.create(command)

                if (result is CreateUserResult.Success) successCount.incrementAndGet()
                if (result is CreateUserResult.DuplicatedEmailFailure) failureCount.incrementAndGet()
                if (result is CreateUserResult.DuplicatedUsernameFailure) failureCount.incrementAndGet()
            }
        }

        latch.countDown()

        executor.shutdown()
        while (!executor.isTerminated) { Thread.sleep(10) }


        assertEquals(1, successCount.get(), "One thread should have succeeded")
        assertEquals(1, failureCount.get(), "One thread should have caught the race condition")
    }
}