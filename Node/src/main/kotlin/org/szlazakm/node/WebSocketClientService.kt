package org.szlazakm.node

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.client.standard.StandardWebSocketClient

@Service
@Slf4j
class WebSocketClientService(private val peerService: PeerService) {
    private val client = StandardWebSocketClient()
    private val logger = LoggerFactory.getLogger(WebSocketClientService::class.java)

    suspend fun connectToPeer(url: String) = withContext(Dispatchers.IO) {
        try {
            logger.info("Connecting to seed: $url")
            client.execute(PeerConnectionHandler(peerService), url)
        } catch (e: Exception) {
            logger.error("Failed to connect to $url: ${e.message}")
        }
    }
}