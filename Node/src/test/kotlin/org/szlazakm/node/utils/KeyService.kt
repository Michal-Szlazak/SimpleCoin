package org.szlazakm.node.utils

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.szlazakm.node.domain.Transaction
import org.szlazakm.node.transaction.Serializer
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64

object KeyService {

    fun generateKeyPair(): KeyPair {
        val keyGen = KeyPairGenerator.getInstance("EC")
        keyGen.initialize(256)
        return keyGen.generateKeyPair()
    }

    fun signTransactionInput(
        privateKeyBytes: ByteArray,
        tx: Transaction,
        inputIndex: Int,
        prevOutputAddress: String
    ): ByteArray {
        val signingData = Serializer.serializeTransactionForSigning(tx, inputIndex, prevOutputAddress)
        val hash = doubleSha256(signingData)

        val keyFactory = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME)
        val privateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(privateKeyBytes))

        val signature = Signature.getInstance("SHA256withECDSA", BouncyCastleProvider.PROVIDER_NAME)
        signature.initSign(privateKey)
        signature.update(hash)
        val signatureBytes = signature.sign()

        return signatureBytes
    }

    private fun doubleSha256(data: ByteArray): ByteArray {
        val sha = MessageDigest.getInstance("SHA-256")
        return sha.digest(sha.digest(data))
    }

    fun sha256Ripemd160(data: ByteArray): String {
        val sha256 = MessageDigest.getInstance("SHA-256").digest(data)
        val ripemd = MessageDigest.getInstance("RIPEMD160")
        val hash = ripemd.digest(sha256)
        return Base64.getEncoder().encodeToString(hash)
    }
}