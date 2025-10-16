package org.szlazakm.node

import kotlinx.coroutines.runBlocking
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

class PeerConnectionHandler(private val peerService: PeerService) : TextWebSocketHandler() {

    override fun afterConnectionEstablished(session: WebSocketSession) {
        runBlocking { peerService.registerIncomingPeer(session) }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        runBlocking { peerService.handleMessage(session, message.payload) }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        runBlocking { peerService.unregisterPeer(session) }
    }
}