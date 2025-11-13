package com.szlazakm.wallet

import com.szlazakm.wallet.wallet.KeyService
import org.assertj.core.api.Assertions.assertThat
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.jupiter.api.Test
import java.security.KeyFactory
import java.security.Security
import java.security.spec.PKCS8EncodedKeySpec

class KeyServiceTest {

    init {
        Security.addProvider(BouncyCastleProvider())
    }

    @Test
    fun `generateKeyPair should return a non-null EC key pair`() {
        val keyPair = KeyService.generateKeyPair()

        assertThat(keyPair).isNotNull
        assertThat(keyPair.private).isNotNull
        assertThat(keyPair.public).isNotNull
        assertThat(keyPair.private.algorithm).isEqualTo("EC")
    }

    @Test
    fun `encrypt and decrypt private key should return original private key bytes`() {
        // Given
        val keyPair = KeyService.generateKeyPair()
        val originalPrivateKey = keyPair.private
        val passphrase = "secure-passphrase"

        // When
        val encrypted = KeyService.encryptPrivateKey(originalPrivateKey, passphrase)
        val decryptedBytes = KeyService.decryptPrivateKey(
            encrypted.encryptedPrivateKey,
            passphrase,
            encrypted.salt,
            encrypted.iv
        )

        val keyFactory = KeyFactory.getInstance("EC")
        val spec = PKCS8EncodedKeySpec(decryptedBytes)
        val restoredPrivateKey = keyFactory.generatePrivate(spec)

        //Then
        assertThat(restoredPrivateKey.encoded).isEqualTo(originalPrivateKey.encoded)
    }

    @Test
    fun `sha256Ripemd160 should return consistent hash for same input`() {
        val input = "hello-world".toByteArray()

        val hash1 = KeyService.sha256Ripemd160(input)
        val hash2 = KeyService.sha256Ripemd160(input)

        assertThat(hash1).isEqualTo(hash2)
        assertThat(hash1).isNotBlank
    }

    @Test
    fun `sha256Ripemd160 should return different hashes for different inputs`() {
        val input1 = "data1".toByteArray()
        val input2 = "data2".toByteArray()

        val hash1 = KeyService.sha256Ripemd160(input1)
        val hash2 = KeyService.sha256Ripemd160(input2)

        assertThat(hash1).isNotEqualTo(hash2)
    }
}
