package com.szlazakm.wallet.transaction

import com.szlazakm.wallet.domain.Transaction
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec

object SignatureUtil {

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
}