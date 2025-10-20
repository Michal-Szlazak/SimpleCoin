package org.szlazakm.node.peer

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.szlazakm.node.config.NodeProperties
import org.szlazakm.node.peer.message.PeerMessageHandler

@Service
class PeerService(
    private val nodeProperties: NodeProperties,
    private val peerRegistry: PeerRegistry,
    private val peerMessageHandler: PeerMessageHandler
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val client = StandardWebSocketClient()

    suspend fun connectToPeer(targetHost: String, targetNodeId: String) {
        if (peerRegistry.hasSessionFor(targetNodeId)) {
            logger.info("Already connected to peer with nodeId: $targetNodeId")
            return
        }

        val targetUri = "ws://$targetHost/ws?nodeId=${nodeProperties.nodeId}"
        logger.info("Connecting to peer at $targetUri ...")

        val future = client.execute(PeerConnectionHandler(this, peerMessageHandler), targetUri)

        runCatching {
            future.get()
        }.onSuccess { session ->
            peerRegistry.registerPeer(targetNodeId, targetHost, session)
            logger.info("Connected to peer: $targetHost as nodeId=${nodeProperties.nodeId}")
        }.onFailure {
            logger.error("Failed to connect to peer at $targetUri", it)
        }
    }

    suspend fun registerIncomingPeer(session: WebSocketSession, nodeId: String) {
        val remoteAddress = session.remoteAddress?.toString()?.removeSurrounding("/") ?: run {
            logger.error("Received null remoteAddress for nodeId: [$nodeId]")
            session.close(CloseStatus.PROTOCOL_ERROR)
            return
        }

        if (peerRegistry.hasSessionFor(nodeId)) {
            logger.warn("Peer $nodeId already registered, closing duplicate session")
            session.close(CloseStatus.PROTOCOL_ERROR)
            return
        }

        peerRegistry.registerPeer(nodeId, remoteAddress, session)
        logger.info("Peer connected: nodeId=$nodeId from $remoteAddress")
    }

    suspend fun unregisterPeer(session: WebSocketSession) {
        logger.info("Unregistering peer: ${session.remoteAddress}")
        peerRegistry.removePeer(session)
    }
}