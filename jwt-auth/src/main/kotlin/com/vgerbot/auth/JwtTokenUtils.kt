package com.vgerbot.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.io.Serializable
import java.util.*
import kotlin.collections.HashMap

@Component
class JwtTokenUtils : Serializable {
    @Autowired
    private lateinit var properties: JwtProperties;

    //retrieve username from jwt token
    fun getUsernameFromToken(token: String?): String? {
        return getClaimFromToken<String>(token, Claims::getSubject)
    }

    //retrieve expiration date from jwt token
    fun getExpirationDateFromToken(token: String?): Date? {
        return getClaimFromToken<Date>(token, Claims::getExpiration)
    }

    fun <T> getClaimFromToken(token: String?, claimsResolver: (claims: Claims) -> T): T {
        val claims: Claims = getAllClaimsFromToken(token)
        return claimsResolver(claims)
    }

    //for retrieveing any information from token we will need the secret key
    private fun getAllClaimsFromToken(token: String?): Claims {
        return Jwts.parser().setSigningKey(properties.secret).parseClaimsJws(token).getBody()
    }

    //check if the token has expired
    private fun isTokenExpired(token: String?): Boolean {
        val expiration: Date? = getExpirationDateFromToken(token)
        return expiration?.before(Date()) ?: false
    }

    //generate token for user
    fun generateToken(userDetails: UserDetails): String {
        val claims: Map<String, Any> = HashMap()
        return doGenerateToken(claims, userDetails.username)
    }

    //while creating the token -
    //1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
    //2. Sign the JWT using the HS512 algorithm and secret key.
    //3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
    //   compaction of the JWT to a URL-safe string
    private fun doGenerateToken(claims: Map<String, Any>, subject: String): String {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
            .signWith(SignatureAlgorithm.HS512, properties.secret).compact()
    }

    //validate token
    fun validateToken(token: String?, userDetails: UserDetails): Boolean {
        val username = getUsernameFromToken(token)
        return username == userDetails.username && !isTokenExpired(token)
    }

    companion object {
        private const val serialVersionUID = -2550185165626007488L
        const val JWT_TOKEN_VALIDITY = (5 * 60 * 60).toLong()
    }
}