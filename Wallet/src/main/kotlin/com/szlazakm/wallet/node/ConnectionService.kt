package com.szlazakm.wallet.node

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient

@Service
class ConnectionService(
    private val nodeConnectionHandler: NodeConnectionHandler,
) {

    private val logger = LoggerFactory.getLogger(ConnectionHandler::class.java)
    private val client = StandardWebSocketClient()
    final var currentSession: WebSocketSession? = null
        private set

    fun connect(host: String) {

        val targetUri = "ws://$host/ws/wallet"
        logger.info("Connecting to node at $targetUri ...")

        val future = client.execute(nodeConnectionHandler, targetUri)

        runCatching {
            future.get()
        }.onSuccess { session ->
            currentSession = session
            logger.info("Connected to peer: $targetUri")
        }.onFailure {
            logger.error("Failed to connect to peer at $targetUri", it)
        }
    }

}