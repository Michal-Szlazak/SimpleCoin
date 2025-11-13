package org.szlazakm.node.wallet.message

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sun.net.httpserver.Authenticator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession
import org.szlazakm.node.domain.BalanceMessage
import org.szlazakm.node.domain.BalancesMessage
import org.szlazakm.node.domain.BaseMessage
import org.szlazakm.node.domain.GetBalanceMessage
import org.szlazakm.node.domain.GetBalancesMessage
import org.szlazakm.node.domain.GetUtxosMessage
import org.szlazakm.node.domain.MessageType
import org.szlazakm.node.domain.TransactionMessage
import org.szlazakm.node.domain.UtxosMessage
import org.szlazakm.node.mempool.MemPool
import org.szlazakm.node.transaction.UTXOService
import org.szlazakm.node.wallet.WalletService
import java.net.URLDecoder

@Component
class WalletMessageHandler(
    private val memPool: MemPool,
    private val walletMessenger: WalletMessenger,
    private val walletService: WalletService,
    private val utxoService: UTXOService
) {

    private val logger = LoggerFactory.getLogger(WalletMessageHandler::class.java)
    private val objectMapper = jacksonObjectMapper()

    suspend fun handle(session: WebSocketSession, payload: String) {
        val message = try {
            objectMapper.readValue(payload, BaseMessage::class.java)
        } catch (e: Exception) {
            logger.error("Invalid message: $payload", e)
            return
        }

        when (message) {
            is TransactionMessage -> {
                logger.info("Received transaction")
                val result = memPool.addTransaction(message.transaction)

                if(result.isFailure) {
                    logger.error("Transaction failed")
                }
            }
            is GetBalanceMessage -> {
                logger.info("Received request to calculate balance for address: ${message.address}")
                val decodedAddress = URLDecoder.decode(message.address, "UTF-8")
                val balance = walletService.getBalance(decodedAddress)
                walletMessenger.send(session, BalanceMessage(MessageType.BALANCE, balance))
            }
            is GetBalancesMessage -> {
                logger.info("Received request to calculate balance for addresses: ${message.addresses}")
                val balances = walletService.getAllBalances(message.addresses)
                walletMessenger.send(session, BalancesMessage(MessageType.BALANCE, balances))
            }
            is GetUtxosMessage -> {

                val utxos = utxoService.getUtxos(message.addresses)

                walletMessenger.send(
                    session,
                    UtxosMessage(MessageType.UTXOS, utxos)
                )
            }
            else -> logger.warn("Unknown message type: ${message.type}. Abandoning transaction.")
        }
    }
}