package org.szlazakm.node.transaction

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.szlazakm.node.config.MinerProperties
import org.szlazakm.node.domain.Transaction
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

@Component
class TransactionValidator(
    private val utxoService: UTXOService,
    private val minerProperties: MinerProperties,
) {

    private final val logger = LoggerFactory.getLogger(javaClass)

    fun validateTransactions(transactions: List<Transaction>): Result<Unit> {

        val spentUtxos: MutableSet<String> = mutableSetOf()

        transactions.forEachIndexed { index, transaction ->

            val validated: Result<Unit> = if(index == 0 && transaction.isCoinbase()) {
                validateCoinbaseTransaction(transaction)
            } else {
                validateInputs(transaction, spentUtxos)
            }

            if(validated.isFailure) {
                return Result.failure(validated.exceptionOrNull()?: Exception("Invalid transaction"))
            }
        }

        return Result.success(Unit)
    }

    fun validateTransaction(transaction: Transaction): Result<Unit> {
        return if (transaction.isCoinbase()) {
            validateCoinbaseTransaction(transaction)
        } else {
            validateInputs(transaction, mutableSetOf())
        }
    }

    private fun validateCoinbaseTransaction(transaction: Transaction): Result<Unit> {

        //TODO: calculate and check the fees
        val result = transaction.outputs[0].value <= minerProperties.reward

        return if(result) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Invalid coinbase transaction"))
        }
    }

    private fun validateInputs(transaction: Transaction, spentUtxos: MutableSet<String>): Result<Unit> {

        val decoder = Base64.getDecoder()
        val recreatedAddress = sha256Ripemd160(decoder.decode(transaction.publicKey))

        transaction.inputs.forEachIndexed { index, txIn ->

            if(spentUtxos.contains("${txIn.txId}:${txIn.outputIndex}")) {
                logger.warn("Transaction input $txIn already spent. Transaction invalid.")
                return Result.failure(Exception("Transaction input $txIn already spent"))
            } else {
                spentUtxos.add("${txIn.txId}:${txIn.outputIndex}")
            }

            val prevOutputAddress = utxoService.getUtxo(txIn.txId)?.address

            if(prevOutputAddress == null) {
                logger.warn("Unspent output not found for input $txIn. Transaction invalid.")
                return Result.failure(Exception("Unspent output not found for input $txIn"))
            }

            if(prevOutputAddress != recreatedAddress) {
                logger.warn("Transaction $txIn references $prevOutputAddress - which is not equal to the address" +
                        " calculated from public key: $prevOutputAddress (recreated address: $recreatedAddress)")
                return Result.failure(Exception("Transaction $txIn references $prevOutputAddress - which is not equal to the address"))
            }

            val signatureValid = validateInputSignature(
                transaction,
                index,
                prevOutputAddress,
                txIn.signature,
                decoder.decode(transaction.publicKey)
            )

            if(!signatureValid) {
                return Result.failure(Exception("Invalid signature"))
            }
        }

        val validateExpenses = validateExpenses(transaction)
        if(validateExpenses.isFailure) {
            return Result.failure(validateExpenses.exceptionOrNull()?: Exception("Invalid expenses"))
        }

        return Result.success(Unit)
    }

    private fun validateExpenses(transaction: Transaction): Result<Unit> {

        var inputSum = 0.0

        transaction.inputs.forEach { transaction ->

            val utxo = utxoService.getUtxo(transaction.txId)
            if(utxo != null) {
                inputSum += utxo.value
            } else {
                return Result.failure(Exception("Invalid transaction"))
            }
        }

        val outputSum = transaction.outputs.sumOf { it.value }

        return if(inputSum == outputSum) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Invalid transaction - output sum is not equal to input sum " +
                    "(out: $outputSum - in: $inputSum)"))
        }
    }

    private fun validateInputSignature(
        tx: Transaction,
        inputIndex: Int,
        prevOutputAddress: String,
        providedSignatureBase64: String,
        providedPublicKeyBytes: ByteArray
    ): Boolean {
        // Step 1: Recreate the same signing data as the wallet
        val signingData = Serializer.serializeTransactionForSigning(tx, inputIndex, prevOutputAddress)
        val hash = doubleSha256(signingData)

        // Step 2: Decode the Base64 signature
        val signatureBytes = Base64.getDecoder().decode(providedSignatureBase64)

        // Step 3: Rebuild the public key
        val keyFactory = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME)
        val publicKey = keyFactory.generatePublic(X509EncodedKeySpec(providedPublicKeyBytes))

        // Step 4: Verify the signature
        val verifier = Signature.getInstance("SHA256withECDSA", BouncyCastleProvider.PROVIDER_NAME)
        verifier.initVerify(publicKey)
        verifier.update(hash)

        return verifier.verify(signatureBytes)
    }

    private fun doubleSha256(data: ByteArray): ByteArray {
        val sha = MessageDigest.getInstance("SHA-256")
        return sha.digest(sha.digest(data))
    }

    private fun sha256Ripemd160(data: ByteArray): String {
        val sha256 = MessageDigest.getInstance("SHA-256").digest(data)
        val ripemd = MessageDigest.getInstance("RIPEMD160")
        val hash = ripemd.digest(sha256)
        return Base64.getEncoder().encodeToString(hash)
    }

}