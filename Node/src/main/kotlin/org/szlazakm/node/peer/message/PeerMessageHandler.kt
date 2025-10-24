package org.szlazakm.node.peer.message

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.WebSocketSession
import org.szlazakm.node.block.Blockchain
import org.szlazakm.node.config.NodeProperties
import org.szlazakm.node.data.BaseMessage
import org.szlazakm.node.data.ChainResponseMessage
import org.szlazakm.node.data.NewBlockMessage
import org.szlazakm.node.data.PeerListMessage
import org.szlazakm.node.data.PingMessage
import org.szlazakm.node.data.PongMessage
import org.szlazakm.node.data.RequestChainMessage
import org.szlazakm.node.peer.PeerRegistry


@Service
class PeerMessageHandler(
    private val peerRegistry: PeerRegistry,
    private val peerMessenger: PeerMessenger,
    private val nodeProperties: NodeProperties,
    private val blockchain: Blockchain,
    private val logger: Logger = LoggerFactory.getLogger(PeerMessageHandler::class.java)
) {

    private val objectMapper = jacksonObjectMapper()

    suspend fun handle(session: WebSocketSession, payload: String) {
        val message = try {
            objectMapper.readValue(payload, BaseMessage::class.java)
        } catch (e: Exception) {
            logger.error("Invalid message: $payload", e)
            return
        }

        when (message) {
            is PingMessage -> {
                logger.info("Received PING")
                peerMessenger.sendPong(session)
            }
            is PongMessage -> {
                logger.info("Received PONG")
            }
            is PeerListMessage -> {
                message.peers.forEach { (id, host) ->
                    if (id == nodeProperties.nodeId) {
                        logger.debug("Skipping self peer ($id)")
                    } else {
                        peerRegistry.registerDiscoveredPeer(id, host)
                    }
                }
            }
            is NewBlockMessage -> {
                logger.info("Received NEW_BLOCK: ${message.block.index}")
                if (blockchain.addBlock(message.block)) {
                    logger.info("Block added to local chain")
                    peerMessenger.broadcast(message) // propagate
                }
            }
            is RequestChainMessage -> {
                logger.info("Received request chain")
                peerMessenger.sendChainResponse(session, blockchain.getAllBlocks())
            }
            is ChainResponseMessage -> {
                logger.info("Received chain response")
                blockchain.resolveChain(message.blocks)
            }
            else -> logger.warn("Unknown message type: ${message.type}")
        }
    }
}
