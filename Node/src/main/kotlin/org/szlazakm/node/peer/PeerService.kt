package org.szlazakm.node.peer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.szlazakm.node.config.NodeProperties
import org.szlazakm.node.data.Message
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

@Service
@Slf4j
class PeerService(
    private val nodeProperties: NodeProperties,
) {


    private val mapper = jacksonObjectMapper()
    private val peers = ConcurrentHashMap<String, String>()
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()
    private val mutex = Mutex()
    private val logger = LoggerFactory.getLogger(PeerService::class.java)

    suspend fun connectToPeer(targetHost: String, targetNodeId: String) {
        if (sessions.containsKey(targetNodeId)) {
            logger.info("Already connected to peer with nodeId: $targetNodeId")
            return
        }

        val client = StandardWebSocketClient()
        val nodeId = nodeProperties.nodeId
        val targetUri = "ws://$targetHost/ws?nodeId=$nodeId"

        logger.info("Connecting to peer at $targetUri ...")

        val handler = PeerConnectionHandler(this)
        val future = client.execute(handler, targetUri)

        // Avoid blocking while holding the mutex
        val session = future.get()

        // Now safely update shared state
        mutex.withLock {
            if (!sessions.containsKey(targetNodeId)) {
                registerOutgoingPeer(session, targetHost, targetNodeId)
                logger.info("Connected to peer: $targetHost as nodeId=$nodeId")
            } else {
                session.close() // Clean up redundant session
                logger.info("Redundant connection attempt to $targetNodeId ignored.")
            }
        }
    }


    suspend fun registerOutgoingPeer(session: WebSocketSession, targetHost: String, targetNodeId: String) {
        mutex.withLock {
            sessions[targetNodeId] = session
            peers[targetNodeId] = targetHost
            logger.info("Outgoing peer registered: nodeId=$targetNodeId, host=$targetHost")
        }
    }

    suspend fun registerIncomingPeer(session: WebSocketSession, nodeId: String) {
        mutex.withLock {
            val remoteAddress = session.remoteAddress ?: run {
                logger.error("Received null remoteAddress for nodeId: [$nodeId]")
                session.close(CloseStatus.PROTOCOL_ERROR)
                return
            }

            if (sessions.containsKey(nodeId)) {
                logger.warn("Peer $nodeId already registered, closing duplicate session")
                session.close(CloseStatus.PROTOCOL_ERROR)
                return
            }

            sessions[nodeId] = session
            peers[nodeId] = remoteAddress.toString().substring(1, remoteAddress.toString().length - 1)
            logger.info("Peer connected: nodeId=$nodeId from $remoteAddress")
        }
    }

    suspend fun unregisterPeer(session: WebSocketSession) {
        logger.info("Unregistering peer: ${session.remoteAddress}")
        mutex.withLock {
            val nodeId = sessions.entries.find { it.value == session }?.key
            if (nodeId != null) {
                sessions.remove(nodeId)
                peers.remove(nodeId)
                logger.info("Peer disconnected: nodeId=$nodeId")
            }
        }
    }

    suspend fun handleMessage(session: WebSocketSession, payload: String) {
        val msg = mapper.readValue(payload, Message::class.java)
        when (msg.type) {
            "PING" ->  {
                logger.info("Received ping")
                session.sendMessage(TextMessage("""{"type":"PONG"}"""))
            }
            "PEER_LIST" -> msg.peers?.forEach {
                if(it.key == nodeProperties.nodeId){
                    logger.info("Received current nodeId as new peer. Skipping.")
                } else if(!peers.keys.contains(it.key)) {
                    logger.info("Received new peer $it")
                    peers[it.key] = it.value
                }
            }
            "PONG" -> {
                logger.info("Received pong")
            }
            else -> logger.info("Unknown message type: ${msg.type}")
        }
    }

    fun getPeers(): Map<String, String> = peers
    fun getPeersWithOpenSessions(): Map<String, String> = sessions.map {
        (key, value) -> key to value.remoteAddress.toString()
    }.toMap()
    fun getOpenSessionsCount(): Int = sessions.size

    fun getPeerWithoutSession(): Map.Entry<String, String>? {
        return peers.entries
            .firstOrNull { it.key !in sessions.keys }
    }




    suspend fun broadcastPeerList() {
        val json = mapper.writeValueAsString(Message("PEER_LIST", peers))
        sessions.values.forEach { session ->
            try {
                session.sendMessage(TextMessage(json))
            } catch (e: Exception) {
                logger.error("Failed to send peer list to ${session.remoteAddress}", e)
            }
        }
    }

    suspend fun broadcastPing() {
        sessions.values.forEach {
            try {
                it.sendMessage(TextMessage("""{"type":"PING"}"""))
            } catch (e: Exception) {
                logger.error("Failed to send PING to ${it.remoteAddress}", e)
            }
        }
    }

}