package org.szlazakm.node.data

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

data class PingMessage(override val type: MessageType = MessageType.PING) : BaseMessage()
data class PongMessage(override val type: MessageType = MessageType.PONG) : BaseMessage()
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