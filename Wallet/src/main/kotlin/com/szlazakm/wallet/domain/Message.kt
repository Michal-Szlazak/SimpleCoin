package com.szlazakm.wallet.domain

import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
sealed class BaseMessage {
    abstract val type: MessageType
}

data class TransactionMessage(
    override val type: MessageType = MessageType.TRANSACTION,
    val transaction: Transaction
) : BaseMessage()

data class GetBalanceMessage(
    override val type: MessageType = MessageType.GET_BALANCE,
    val address: String
) : BaseMessage()

data class GetBalancesMessage(
    override val type: MessageType = MessageType.GET_BALANCES,
    val addresses: List<String>
) : BaseMessage()

data class BalanceMessage(
    override val type: MessageType = MessageType.BALANCE,
    val address: String,
    val balance: Double
) : BaseMessage()

data class BalancesMessage(
    override val type: MessageType = MessageType.BALANCES,
    val balances: Map<String, Double>
) : BaseMessage()

data class GetUtxosMessage(
    override val type: MessageType = MessageType.GET_UTXOS,
    val addresses: List<String>
) : BaseMessage()

data class UtxosMessage(
    override val type: MessageType = MessageType.UTXOS,
    val utxos: Map<String, TxOutput>
) : BaseMessage()
