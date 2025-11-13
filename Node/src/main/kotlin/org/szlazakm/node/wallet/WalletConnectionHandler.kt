package org.szlazakm.node.wallet

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import org.szlazakm.node.peer.PeerConnectionHandler
import org.szlazakm.node.peer.message.PeerMessageHandler
import org.szlazakm.node.wallet.message.WalletMessageHandler

@Component
class WalletConnectionHandler(
    private val walletMessageHandler: WalletMessageHandler
) : TextWebSocketHandler(){

    private val log = LoggerFactory.getLogger(PeerConnectionHandler::class.java)
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        log.info("Connection established with new wallet.")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        coroutineScope.launch {
            try {
                walletMessageHandler.handle(session, message.payload)
            } catch (e: Exception) {
                log.error("Error handling message from wallet ${session.remoteAddress}", e)
            }
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        log.info("Connection closed with wallet.")
    }

}