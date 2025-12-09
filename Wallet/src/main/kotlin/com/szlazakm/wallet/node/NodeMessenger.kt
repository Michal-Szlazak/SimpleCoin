package com.szlazakm.wallet.node

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.szlazakm.wallet.domain.BaseMessage
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession

@Component
class NodeMessenger(
    private val connectionService: ConnectionService,
) {

    private val mapper = jacksonObjectMapper()
    private val logger = LoggerFactory.getLogger(NodeMessenger::class.java)
    private val sendLock = Mutex()

    suspend fun send(message: BaseMessage) {

        val session = connectionService.currentSession

        sendLock.withLock {
            session?.let {
                val json = mapper.writeValueAsString(message)
                send(session, json)
            } ?: run {
                logger.error("Could not send message. There is no active session.")
            }
        }


    }

    private suspend fun send(session: WebSocketSession, json: String) {
        try {
            session.sendMessage(TextMessage(json))
        } catch (e: Exception) {
            logger.error("Failed to send message to ${session.remoteAddress}", e)
        }
    }
}