package com.szlazakm.wallet.domain

data class TxInput(
    val txId: String,
    val outputIndex: Int,
    var signature: ByteArray
)

data class TxOutput(
    val value: Double,
    val address: String
)

data class Transaction(
    val id: String,
    val inputs: List<TxInput>,
    val outputs: List<TxOutput>,
    val publicKey: String
)