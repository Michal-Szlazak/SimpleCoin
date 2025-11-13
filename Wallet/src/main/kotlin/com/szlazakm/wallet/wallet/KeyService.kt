package com.szlazakm.wallet.wallet

import org.springframework.stereotype.Service
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

@Service
object KeyService {

    private const val EC = "EC"
    const val TAG_LENGTH_BIT = 128
    const val IV_LENGTH_BYTE = 12
    const val SALT_LENGTH_BYTE = 16;
    const val KEY_LENGTH_BIT = 256
    private const val AES_MODE = "AES/GCM/NoPadding"

    fun generateKeyPair(): KeyPair {
        val keyGen = KeyPairGenerator.getInstance(EC)
        keyGen.initialize(256)
        return keyGen.generateKeyPair()
    }

    fun encryptPrivateKey(privateKey: PrivateKey, passphrase: String): EncryptionData {
        val salt = ByteArray(SALT_LENGTH_BYTE)
        SecureRandom().nextBytes(salt)

        val secretKey = deriveKey(passphrase, salt)

        val iv = ByteArray(IV_LENGTH_BYTE)
        SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance(AES_MODE)
        val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
        val encrypted = cipher.doFinal(privateKey.encoded)

        return EncryptionData(
            iv = Base64.getEncoder().encodeToString(iv),
            salt = Base64.getEncoder().encodeToString(salt),
            encryptedPrivateKey = Base64.getEncoder().encodeToString(encrypted)
        )
    }


    fun decryptPrivateKey(encrypted: String, passphrase: String, salt: String, iv: String): ByteArray {
        val cipherText = Base64.getDecoder().decode(encrypted)
        val ivBytes = Base64.getDecoder().decode(iv)
        val saltBytes = Base64.getDecoder().decode(salt)
        val secretKey = deriveKey(passphrase, saltBytes)

        val cipher = Cipher.getInstance(AES_MODE)
        val spec = GCMParameterSpec(TAG_LENGTH_BIT, ivBytes)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher.doFinal(cipherText)
    }


    private fun deriveKey(passphrase: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(passphrase.toCharArray(), salt, 100_000, KEY_LENGTH_BIT)
        val key = factory.generateSecret(spec).encoded
        return SecretKeySpec(key, "AES")
    }

    fun sha256Ripemd160(data: ByteArray): String {
        val sha256 = MessageDigest.getInstance("SHA-256").digest(data)
        val ripemd = MessageDigest.getInstance("RIPEMD160")
        val hash = ripemd.digest(sha256)
        return Base64.getEncoder().encodeToString(hash)
    }

}

data class EncryptionData(
    val iv: String,
    val salt: String,
    val encryptedPrivateKey: String,
)