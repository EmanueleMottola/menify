package com.xazy.menify.infrastructure.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtService(
    @Value("\${jwt.secret}") private val secret: String
) {
    private val key = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generate(userId: String): String = Jwts.builder()
        .subject(userId)
        .issuedAt(Date())
        .expiration(Date(System.currentTimeMillis() + 86_400_000)) // 24h
        .signWith(key)
        .compact()

    fun extract(token: String): String = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .payload
        .subject
}