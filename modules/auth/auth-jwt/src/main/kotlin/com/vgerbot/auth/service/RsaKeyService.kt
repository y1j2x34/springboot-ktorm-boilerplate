package com.vgerbot.auth.service

import com.vgerbot.auth.exception.KeyExpiredException
import com.vgerbot.auth.exception.KeyNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import java.util.concurrent.TimeUnit

/**
 * RSA 密钥服务
 * 
 * 提供基于会话的短期 RSA 密钥对生成和管理功能：
 * - 为每个会话生成独立的密钥对
 * - 私钥存储在 Redis 中，5分钟 TTL
 * - 用后即焚机制：使用后立即删除私钥
 * - 密钥ID格式: session_{sessionId}_{timestamp}
 */
@Service
class RsaKeyService(
    private val stringRedisTemplate: StringRedisTemplate
) {
    private val logger = LoggerFactory.getLogger(RsaKeyService::class.java)
    
    companion object {
        private const val RSA_KEY_PREFIX = "rsa_private_key:"
        private const val KEY_TTL_SECONDS = 300L // 5分钟
        private const val KEY_SIZE = 2048
        private const val ALGORITHM = "RSA"
    }
    
    /**
     * 生成密钥对
     * 
     * @param sessionId 会话ID
     * @return 密钥信息（keyId, publicKey, expiresAt, algorithm）
     */
    fun generateKeyPair(sessionId: String?): RsaKeyInfo {
        val actualSessionId = sessionId ?: generateSessionId()
        val timestamp = System.currentTimeMillis()
        val keyId = "session_${actualSessionId}_${timestamp}"
        
        try {
            // 生成 RSA 密钥对
            val keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM)
            keyPairGenerator.initialize(KEY_SIZE)
            val keyPair = keyPairGenerator.generateKeyPair()
            
            // 将公钥和私钥转换为 PEM 格式
            val publicKeyPem = encodePublicKey(keyPair.public)
            val privateKeyPem = encodePrivateKey(keyPair.private)
            
            // 存储私钥到 Redis，设置5分钟过期时间
            val redisKey = RSA_KEY_PREFIX + keyId
            stringRedisTemplate.opsForValue().set(redisKey, privateKeyPem, KEY_TTL_SECONDS, TimeUnit.SECONDS)
            
            val expiresAt = System.currentTimeMillis() + (KEY_TTL_SECONDS * 1000)
            
            logger.debug("Generated RSA key pair for session: {}, keyId: {}", actualSessionId, keyId)
            
            return RsaKeyInfo(
                keyId = keyId,
                publicKey = publicKeyPem,
                expiresAt = expiresAt,
                algorithm = ALGORITHM
            )
        } catch (e: Exception) {
            logger.error("Failed to generate RSA key pair", e)
            throw RuntimeException("密钥生成失败", e)
        }
    }
    
    /**
     * 获取私钥并解密密码
     * 
     * @param keyId 密钥ID
     * @param encryptedPassword Base64编码的加密密码
     * @return 解密后的密码
     * @throws KeyNotFoundException 密钥不存在
     * @throws KeyExpiredException 密钥已过期
     */
    fun decryptPassword(keyId: String, encryptedPassword: String): String {
        val redisKey = RSA_KEY_PREFIX + keyId
        
        // 获取私钥（原子操作：获取并删除）
        val privateKeyPem = stringRedisTemplate.opsForValue().getAndDelete(redisKey)
            ?: throw KeyNotFoundException("密钥不存在或已过期: $keyId")
        
        try {
            // 解析私钥
            val privateKey = decodePrivateKey(privateKeyPem)
            
            // 解密密码
            val cipher = javax.crypto.Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, privateKey)
            
            val encryptedBytes = Base64.getDecoder().decode(encryptedPassword)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            val password = String(decryptedBytes, Charsets.UTF_8)
            
            logger.debug("Successfully decrypted password using keyId: {}", keyId)
            
            return password
        } catch (e: KeyNotFoundException) {
            throw e
        } catch (e: Exception) {
            logger.error("Failed to decrypt password with keyId: {}", keyId, e)
            throw RuntimeException("密码解密失败", e)
        }
    }
    
    /**
     * 检查密钥是否存在
     * 
     * @param keyId 密钥ID
     * @return 是否存在
     */
    fun keyExists(keyId: String): Boolean {
        val redisKey = RSA_KEY_PREFIX + keyId
        return stringRedisTemplate.hasKey(redisKey) == true
    }
    
    /**
     * 删除密钥（用后即焚）
     * 
     * @param keyId 密钥ID
     */
    fun deleteKey(keyId: String) {
        val redisKey = RSA_KEY_PREFIX + keyId
        stringRedisTemplate.delete(redisKey)
        logger.debug("Deleted RSA key: {}", keyId)
    }
    
    /**
     * 将公钥编码为 PEM 格式
     */
    private fun encodePublicKey(publicKey: PublicKey): String {
        val keyFactory = KeyFactory.getInstance(ALGORITHM)
        val keySpec = keyFactory.getKeySpec(publicKey, X509EncodedKeySpec::class.java)
        val encoded = Base64.getEncoder().encodeToString(keySpec.encoded)
        return "-----BEGIN PUBLIC KEY-----\n${encoded.chunked(64).joinToString("\n")}\n-----END PUBLIC KEY-----"
    }
    
    /**
     * 将私钥编码为 PEM 格式
     */
    private fun encodePrivateKey(privateKey: PrivateKey): String {
        val keyFactory = KeyFactory.getInstance(ALGORITHM)
        val keySpec = keyFactory.getKeySpec(privateKey, PKCS8EncodedKeySpec::class.java)
        val encoded = Base64.getEncoder().encodeToString(keySpec.encoded)
        return "-----BEGIN PRIVATE KEY-----\n${encoded.chunked(64).joinToString("\n")}\n-----END PRIVATE KEY-----"
    }
    
    /**
     * 从 PEM 格式解码私钥
     */
    private fun decodePrivateKey(privateKeyPem: String): PrivateKey {
        val privateKeyContent = privateKeyPem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\n", "")
            .replace(" ", "")
        
        val keyBytes = Base64.getDecoder().decode(privateKeyContent)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance(ALGORITHM)
        return keyFactory.generatePrivate(keySpec)
    }
    
    /**
     * 生成会话ID
     */
    private fun generateSessionId(): String {
        return java.util.UUID.randomUUID().toString().replace("-", "")
    }
}

/**
 * RSA 密钥信息
 */
data class RsaKeyInfo(
    val keyId: String,
    val publicKey: String,
    val expiresAt: Long,
    val algorithm: String
)

