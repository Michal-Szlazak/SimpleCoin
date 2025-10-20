package org.szlazakm.node.peer

import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Component
class PeerRegistry {

    private val sessions = ConcurrentHashMap<String, WebSocketSession>() // nodeId -> session
    private val peers = ConcurrentHashMap<String, String>() // nodeId -> host
    private val mutex = Mutex()

    suspend fun registerPeer(nodeId: String, host: String, session: WebSocketSession) {
        mutex.withLock {
            sessions[nodeId] = session
            peers[nodeId] = host
        }
    }

    suspend fun removePeer(session: WebSocketSession) {
        mutex.withLock {
            val nodeId = sessions.entries.find { it.value == session }?.key
            if (nodeId != null) {
                sessions.remove(nodeId)
                peers.remove(nodeId)
            }
        }
    }

    fun getPeersWithOpenSessions(): Map<String, String> {

        return sessions.mapValues { (_, session) ->
            session.remoteAddress?.toString() ?: "unknown"
        }
    }


    suspend fun getPeerWithoutSession(): Map.Entry<String, String>? {
        mutex.withLock {
            return peers.entries.firstOrNull { !sessions.containsKey(it.key) }
        }
    }

    suspend fun registerDiscoveredPeer(nodeId: String, host: String) {
        mutex.withLock {
            if (!peers.containsKey(nodeId)) {
                peers[nodeId] = host
            }
        }
    }


    fun getSession(nodeId: String): WebSocketSession? = sessions[nodeId]
    fun getPeers(): Map<String, String> = peers
    fun getSessions(): Collection<WebSocketSession> = sessions.values
    fun getOpenSessionsCount(): Int = sessions.size
    fun hasSessionFor(nodeId: String): Boolean = sessions.containsKey(nodeId)
}
