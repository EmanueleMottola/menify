package com.xazy.menify.infrastructure.postgres

import com.xazy.menify.domain.CreateUserResult
import com.xazy.menify.domain.User
import com.xazy.menify.domain.UsersRepository
import com.xazy.menify.domain.CreateUserCommand
import org.springframework.stereotype.Component
import org.jooq.DSLContext
import org.jooq.generated.Tables.USERS
import org.jooq.generated.tables.records.UsersRecord
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

@Component
class PostgresUsersRepository(
    private val dslContext: DSLContext,
    transactionManager: PlatformTransactionManager
) : UsersRepository {

    private val transactionTemplate = TransactionTemplate(transactionManager)
    private val logger = LoggerFactory.getLogger(PostgresUsersRepository::class.java);

    override fun getUsers(): List<User> {
        val users = dslContext.selectFrom(USERS)
            .fetch()
            .map { User(id = it.userId.toString(), username = it.username) }
        return users
    }

    override fun create(user: CreateUserCommand): CreateUserResult {
        logger.info("Saving user to the database.")
        val result = transactionTemplate.execute {
            val alreadyExistingUser = fetchUserWithSameEmailOrUsernameIfExists(user)

            if (alreadyExistingUser != null) {
                if (user.email == alreadyExistingUser.email) {
                    logger.info("User's email already exists, skipping.")
                    return@execute CreateUserResult.DuplicatedEmailFailure
                }
                if (user.username == alreadyExistingUser.username) {
                    logger.info("User's username already exists, skipping.")
                    return@execute CreateUserResult.DuplicatedUsernameFailure
                }
            }

            return@execute try {
                dslContext.insertInto(USERS)
                    .columns(USERS.EMAIL, USERS.USERNAME, USERS.PASSWORD)
                    .values(user.email, user.username, user.password)
                    .returning(USERS.USER_ID, USERS.USERNAME)
                    .fetchOne()
                    ?.let { CreateUserResult.Success(User(id = it.userId.toString(), username = it.username)) }
                    ?: throw IllegalStateException("Insert returned no record")
            } catch (ex: DataIntegrityViolationException) {
                val msg = ex.mostSpecificCause.message ?: ""

                when {
                    msg.contains("users_email_key") -> {
                        logger.error("Saving user was not possible because of email constraints violation.")
                        return@execute CreateUserResult.DuplicatedEmailFailure
                    }
                    msg.contains("users_username_key") -> {
                        logger.error("Saving user was not possible because of username constraints violation.")
                        return@execute CreateUserResult.DuplicatedUsernameFailure
                    }
                    else -> {
                        logger.error("Unknown constraint violation happened.")
                        throw ex
                    }
                }
            }
        }

        if (result is CreateUserResult.Success) {
            logger.info("Saving user was successful.")
        }

        return result
    }

    private fun fetchUserWithSameEmailOrUsernameIfExists(user: CreateUserCommand): UsersRecord? = dslContext.selectFrom(USERS)
        .where(USERS.EMAIL.eq(user.email))
        .or(USERS.USERNAME.eq(user.username))
        .fetchOne()
}
