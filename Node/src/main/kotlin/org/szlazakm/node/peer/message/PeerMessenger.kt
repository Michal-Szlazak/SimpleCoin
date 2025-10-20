package org.szlazakm.node.peer.message

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.szlazakm.node.data.Message
import org.szlazakm.node.peer.PeerRegistry

@Service
class PeerMessenger(
    private val peerRegistry: PeerRegistry,
    private val logger: Logger = LoggerFactory.getLogger(PeerMessenger::class.java)
) {
    private val mapper = jacksonObjectMapper()

    suspend fun sendPong(session: WebSocketSession) {
        send(session, mapOf("type" to "PONG"))
    }

    suspend fun sendPing(session: WebSocketSession) {
        send(session, mapOf("type" to "PING"))
    }

    suspend fun broadcastPeerList() {
        val peers = peerRegistry.getPeers()
        val msg = Message("PEER_LIST", peers)
        val json = mapper.writeValueAsString(msg)
        broadcast(json)
    }

    suspend fun broadcastPing() {
        broadcast(mapOf("type" to "PING"))
    }

    private suspend fun broadcast(json: String) {
        peerRegistry.getSessions().forEach { session ->
            send(session, json)
        }
    }

    private suspend fun broadcast(message: Map<String, Any>) {
        val json = mapper.writeValueAsString(message)
        broadcast(json)
    }

    private suspend fun send(session: WebSocketSession, message: Map<String, Any>) {
        send(session, jacksonObjectMapper().writeValueAsString(message))
    }

    private suspend fun send(session: WebSocketSession, json: String) {
        try {
            session.sendMessage(TextMessage(json))
        } catch (e: Exception) {
            logger.error("Failed to send message to ${session.remoteAddress}", e)
        }
    }
}