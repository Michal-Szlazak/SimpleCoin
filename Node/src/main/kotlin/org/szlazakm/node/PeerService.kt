package org.szlazakm.node

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.sync.Mutex
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.szlazakm.node.data.Message
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.sync.withLock

@Service
class PeerService {

    private val mapper = jacksonObjectMapper()
    private val peers = ConcurrentHashMap.newKeySet<String>()
    private val sessions = ConcurrentHashMap.newKeySet<WebSocketSession>()
    private val mutex = Mutex()

    suspend fun registerIncomingPeer(session: WebSocketSession) {
        mutex.withLock {
            peers.add(session.remoteAddress?.toString() ?: "unknown")
            sessions.add(session)
            println("Incoming peer connected: ${session.remoteAddress}")
        }
    }

    suspend fun unregisterPeer(session: WebSocketSession) {
        mutex.withLock {
            peers.remove(session.remoteAddress?.toString())
            sessions.remove(session)
            println("Peer disconnected: ${session.remoteAddress}")
        }
    }

    suspend fun handleMessage(session: WebSocketSession, payload: String) {
        val msg = mapper.readValue(payload, Message::class.java)
        when (msg.type) {
            "PING" -> session.sendMessage(TextMessage("""{"type":"PONG"}"""))
            "PEER_LIST" -> msg.peers?.forEach { peers.add(it) }
            else -> println("Received message: $payload")
        }
    }

    fun getPeers(): Set<String> = peers

    suspend fun broadcastPeerList() {
        val json = mapper.writeValueAsString(Message("PEER_LIST", peers.toList()))
        sessions.forEach { session ->
            try {
                session.sendMessage(TextMessage(json))
            } catch (e: Exception) {
                println("Broadcast failed: ${e.message}")
            }
        }
    }

}