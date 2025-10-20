package org.szlazakm.node.peer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import org.szlazakm.node.peer.message.PeerMessageHandler

@Component
@Slf4j
class PeerConnectionHandler(
    private val peerService: PeerService,
    private val peerMessageHandler: PeerMessageHandler
) : TextWebSocketHandler() {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val log = LoggerFactory.getLogger(PeerConnectionHandler::class.java)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val nodeId = session.attributes["nodeId"] as? String

        nodeId?.let { id ->
            coroutineScope.launch {
                try {
                    peerService.registerIncomingPeer(session, id)
                } catch (e: Exception) {
                    log.error("Error registering incoming peer for nodeId=$id", e)
                    session.close(CloseStatus.SERVER_ERROR)
                }
            }
        }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        coroutineScope.launch {
            try {
                peerMessageHandler.handle(session, message.payload)
            } catch (e: Exception) {
                log.error("Error handling message from ${session.remoteAddress}", e)
            }
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        coroutineScope.launch {
            try {
                peerService.unregisterPeer(session)
            } catch (e: Exception) {
                log.error("Error unregistering peer for session ${session.remoteAddress}", e)
            }
        }
    }
}