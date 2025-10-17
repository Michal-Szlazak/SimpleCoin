package org.szlazakm.node.peer

import kotlinx.coroutines.runBlocking
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import org.szlazakm.node.peer.PeerService

@Component
@Slf4j
class PeerConnectionHandler(private val peerService: PeerService) : TextWebSocketHandler() {

    private val logger: Logger = LoggerFactory.getLogger(PeerConnectionHandler::class.java)

    override fun afterConnectionEstablished(session: WebSocketSession) {

        val nodeId = session.attributes["nodeId"] as? String

        nodeId?.let {
            runBlocking { peerService.registerIncomingPeer(session, nodeId) }
        }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        runBlocking { peerService.handleMessage(session, message.payload) }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        runBlocking { peerService.unregisterPeer(session) }
    }
}