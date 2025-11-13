package com.szlazakm.wallet.transaction

import com.szlazakm.wallet.wallet.PersistenceService
import com.szlazakm.wallet.domain.GetBalanceMessage
import com.szlazakm.wallet.domain.GetBalancesMessage
import com.szlazakm.wallet.domain.MessageType
import com.szlazakm.wallet.domain.Transaction
import com.szlazakm.wallet.domain.TransactionMessage
import com.szlazakm.wallet.node.ConnectionService
import com.szlazakm.wallet.node.NodeMessenger
import com.szlazakm.wallet.wallet.KeyService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TransactionService(
    private val persistenceService: PersistenceService,
    private val nodeMessenger: NodeMessenger,
    private val utxoService: UTXOService
) {

    private val logger = LoggerFactory.getLogger(TransactionService::class.java)

    suspend fun getBalance(address: String) {

        nodeMessenger.send(
            GetBalanceMessage(
                MessageType.GET_BALANCE,
                address,
            )
        )

    }

    suspend fun getAllBalances()     {
        val identities = persistenceService.retrieveAll()
        val addresses = identities.map { it.address }

        nodeMessenger.send(
            GetBalancesMessage(
                MessageType.GET_BALANCES,
                addresses,
            )
        )

    }

    suspend fun sendTransaction(transaction: Transaction, privateKeyBytes: ByteArray) {

        transaction.inputs.forEachIndexed { i, input ->

            val utxos = utxoService.getCurrentUtxos()
            val prevUtxos = utxos[input.txId]

            prevUtxos?.let {

                val signature = SignatureUtil.signTransactionInput(
                    privateKeyBytes = privateKeyBytes,
                    tx = transaction,
                    inputIndex = i,
                    prevOutputAddress = prevUtxos.address
                )

                input.signature = signature
            } ?: run {
                logger.error("Failed to sign transaction input: $input. ")
                return
            }


        }
        nodeMessenger.send(
            TransactionMessage(
                MessageType.TRANSACTION,
                transaction,
            )
        )
    }

}