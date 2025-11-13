package org.szlazakm.node.wallet.message

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.szlazakm.node.domain.BaseMessage

@Service
class WalletMessenger {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val mapper = jacksonObjectMapper()

    suspend fun send(session: WebSocketSession, message: BaseMessage) {
        val json = mapper.writeValueAsString(message)
        send(session, json)
    }

    private suspend fun send(session: WebSocketSession, json: String) {
        try {
            session.sendMessage(TextMessage(json))
        } catch (e: Exception) {
            logger.error("Failed to send message to ${session.remoteAddress}", e)
        }
    }
}