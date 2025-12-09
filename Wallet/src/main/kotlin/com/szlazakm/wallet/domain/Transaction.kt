package com.szlazakm.wallet.domain

data class TxInput(
    val txId: String,
    val outputIndex: Int,
    var sigScript: String
)

data class TxOutput(
    val value: Double,
    val address: String
)

data class Transaction(
    var id: String,
    val inputs: List<TxInput>,
    val outputs: List<TxOutput>
)