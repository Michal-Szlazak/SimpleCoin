package com.szlazakm.wallet.node

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.szlazakm.wallet.domain.BalanceMessage
import com.szlazakm.wallet.domain.BalancesMessage
import com.szlazakm.wallet.domain.BaseMessage
import com.szlazakm.wallet.domain.UtxosMessage
import com.szlazakm.wallet.transaction.UTXOService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class NodeMessageHandler(
    private val utxoService: UTXOService
) {

    private val objectMapper = jacksonObjectMapper()
    private val logger = LoggerFactory.getLogger(NodeMessageHandler::class.java)

    fun handle(session: WebSocketSession, payload: String) {

        val message = try {
            objectMapper.readValue(payload, BaseMessage::class.java)
        } catch (e: Exception) {
            logger.error("Invalid message: $payload", e)
            return
        }

        when (message) {

            is BalanceMessage -> {
                logger.info("Balance: ${message.balance}")
            }
            is BalancesMessage -> {
                logger.info("Balances: ${message.balances}")
            }
            is UtxosMessage -> {
                logger.debug("Utxos: {}", message.utxos)
                utxoService.updateCurrentUtxos(message.utxos)
            }
            else -> {
                logger.error("Invalid message: $payload")
            }
        }
    }

}