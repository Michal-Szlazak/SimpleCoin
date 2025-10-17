package org.szlazakm.node

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.szlazakm.node.config.NodeProperties
import org.szlazakm.node.peer.PeerConnectionHandler
import org.szlazakm.node.peer.PeerService

@Service
@Slf4j
class WebSocketClientService(
    private val peerService: PeerService,
    private val nodeProperties: NodeProperties,
) {
    private val client = StandardWebSocketClient()
    private val logger = LoggerFactory.getLogger(WebSocketClientService::class.java)

    fun connectToPeer(peerUrl: String) {
        val urlWithId = "$peerUrl?nodeId=${nodeProperties.nodeId}"
        logger.info("Connecting to peer: $urlWithId")
        try {
            client.execute(PeerConnectionHandler(peerService), urlWithId)
        } catch (e: Exception) {
            logger.error("Handshake failed: ${e.message}")
        }
    }
}