package org.szlazakm.node.domain

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = PingMessage::class, name = "PING"),
    JsonSubTypes.Type(value = PongMessage::class, name = "PONG"),
    JsonSubTypes.Type(value = PeerListMessage::class, name = "PEER_LIST")
)
sealed class BaseMessage {
    abstract val type: MessageType
}

data class PingMessage(
    override val type: MessageType = MessageType.PING
) : BaseMessage()

data class PongMessage(
    override val type: MessageType = MessageType.PONG
) : BaseMessage()

data class PeerListMessage(
    override val type: MessageType = MessageType.PEER_LIST,
    val peers: Map<String, String>
) : BaseMessage()

data class NewBlockMessage(
    override val type: MessageType = MessageType.NEW_BLOCK,
    val block: Block
) : BaseMessage()

data class RequestChainMessage(
    override val type: MessageType = MessageType.REQUEST_CHAIN
) : BaseMessage()

data class ChainResponseMessage(
    override val type: MessageType = MessageType.CHAIN_RESPONSE,
    val blocks: List<Block>
) : BaseMessage()

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