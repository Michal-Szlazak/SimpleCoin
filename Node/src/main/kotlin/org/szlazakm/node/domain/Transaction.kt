package org.szlazakm.node.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import org.szlazakm.node.block.jpa.BlockEntity
import org.szlazakm.node.block.jpa.TransactionEntity
import org.szlazakm.node.block.jpa.TxInputEntity
import org.szlazakm.node.block.jpa.TxOutputEntity
import java.security.MessageDigest

data class TxInput(
    val txId: String,
    val outputIndex: Int,
    var sigScript: String
) {
    fun toEntity(tx: TransactionEntity) =
        TxInputEntity(
            txId = txId,
            outputIndex = outputIndex,
            sigScript = sigScript,
            transaction = tx
        )
}

data class TxOutput(
    val value: Double,
    val address: String
) {
    fun toEntity(tx: TransactionEntity, index: Int) =
        TxOutputEntity(
            amount = value,
            address = address,
            outputIndex = index,
            transaction = tx
        )
}

data class Transaction(
    val id: String,
    val inputs: List<TxInput>,
    val outputs: List<TxOutput>
) {

    companion object {
        fun calculateHash(inputs: List<TxInput>, outputs: List<TxOutput>, publicKey: String): String {

            val inputData = inputs.joinToString(separator = "|") { "${it.txId}:${it.outputIndex}" }
            val outputData = outputs.joinToString(separator = "|") { "${it.address}:${it.value}" }
            val data = "$inputData|$outputData|$publicKey"

            return sha256(data)
        }

        private fun sha256(data: String): String {
            val digest = MessageDigest.getInstance("SHA-256").digest(data.toByteArray())
            return digest.joinToString("") { "%02x".format(it) }
        }
    }

    @JsonIgnore
    fun isCoinbase(): Boolean {
        if(inputs.size != 1) return false
        if(outputs.size != 1) return false
        if(!inputs[0].sigScript.startsWith("coinbase_")) return false
        return true
    }

    fun toEntity(block: BlockEntity): TransactionEntity {
        val txEntity = TransactionEntity(
            id = id,
            inputs = mutableListOf(),
            outputs = mutableListOf(),
            block = block,
        )

        val inputsEntity = inputs.map { it.toEntity(txEntity) }
        val outputsEntity = outputs.mapIndexed { index, out ->
            out.toEntity(txEntity, index)
        }

        return txEntity.copy(
            inputs = inputsEntity.toMutableList(),
            outputs = outputsEntity.toMutableList(),
        )
    }
}
