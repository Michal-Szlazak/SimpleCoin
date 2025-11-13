package com.szlazakm.wallet.node

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class ConnectionHandler(): TextWebSocketHandler() {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val log = LoggerFactory.getLogger(ConnectionHandler::class.java)

    override fun afterConnectionEstablished(session: WebSocketSession) {

    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {

    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {

    }

}