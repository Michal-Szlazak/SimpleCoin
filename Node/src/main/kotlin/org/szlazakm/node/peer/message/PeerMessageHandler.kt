package org.szlazakm.node.peer.message

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.WebSocketSession
import org.szlazakm.node.config.NodeProperties
import org.szlazakm.node.data.Message
import org.szlazakm.node.peer.PeerRegistry


@Service
class PeerMessageHandler(
    private val peerRegistry: PeerRegistry,
    private val peerMessenger: PeerMessenger,
    private val nodeProperties: NodeProperties,
    private val logger: Logger = LoggerFactory.getLogger(PeerMessageHandler::class.java)
) {

    suspend fun handle(session: WebSocketSession, payload: String) {
        val message = try {
            jacksonObjectMapper().readValue(payload, Message::class.java)
        } catch (e: Exception) {
            logger.error("Invalid message: $payload", e)
            return
        }

        when (message.type) {
            "PING" -> {
                logger.info("Received PING")
                peerMessenger.sendPong(session)
            }

            "PONG" -> logger.info("Received PONG")

            "PEER_LIST" -> {
                message.peers?.forEach { (id, host) ->
                    if (id == nodeProperties.nodeId) {
                        logger.debug("Skipping self peer ($id)")
                    } else {
                        peerRegistry.registerDiscoveredPeer(id, host)
                    }
                }
            }

            else -> logger.warn("Unknown message type: ${message.type}")
        }
    }
}
