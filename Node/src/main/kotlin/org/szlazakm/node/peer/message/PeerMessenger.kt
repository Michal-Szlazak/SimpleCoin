package org.szlazakm.node.peer.message

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.szlazakm.node.data.*
import org.szlazakm.node.peer.PeerRegistry

@Service
class PeerMessenger(
    private val peerRegistry: PeerRegistry,
    private val logger: Logger = LoggerFactory.getLogger(PeerMessenger::class.java)
) {
    private val mapper = jacksonObjectMapper()

    // === BASIC PING/PONG ===
    suspend fun sendPing(session: WebSocketSession) = send(session, PingMessage())
    suspend fun sendPong(session: WebSocketSession) = send(session, PongMessage())

    suspend fun broadcastPing() = broadcast(PingMessage())

    // === PEER LIST ===
    suspend fun broadcastPeerList() {
        val peers = peerRegistry.getPeers()
        val message = PeerListMessage(peers = peers)
        broadcast(message)
    }

    // === BLOCKCHAIN SYNC ===
    suspend fun broadcastNewBlock(block: Block) = broadcast(NewBlockMessage(block = block))

    suspend fun sendRequestChain(session: WebSocketSession) =
        send(session, RequestChainMessage())

    suspend fun sendChainResponse(session: WebSocketSession, blocks: List<Block>) =
        send(session, ChainResponseMessage(blocks = blocks))

    // === GENERIC SEND / BROADCAST ===
    suspend fun broadcast(message: BaseMessage) {
        val json = mapper.writeValueAsString(message)
        peerRegistry.getSessions().forEach { session ->
            send(session, json)
        }
    }

    suspend fun send(session: WebSocketSession, message: BaseMessage) {
        val json = mapper.writeValueAsString(message)
        send(session, json)
    }

    private suspend fun send(session: WebSocketSession, json: String) {
        try {
            session.sendMessage(TextMessage(json))
        } catch (e: Exception) {
            logger.error("Failed to send message to ${session.remoteAddress}", e)
        }
    }
}
