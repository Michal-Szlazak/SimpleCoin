package com.szlazakm.wallet.transaction

import com.szlazakm.wallet.domain.GetUtxosMessage
import com.szlazakm.wallet.domain.MessageType
import com.szlazakm.wallet.node.NodeMessenger
import com.szlazakm.wallet.wallet.PersistenceService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class UTXOScheduler(
    private val nodeMessenger: NodeMessenger,
    private val persistenceService: PersistenceService,
) {

    private val logger = LoggerFactory.getLogger(UTXOScheduler::class.java)
    private val scope = CoroutineScope(Dispatchers.IO)

    @Scheduled(fixedRate = 15_000)
    fun requestCurrentUtxos() {

        logger.info("Requesting current utxos")

        val identities = persistenceService.retrieveAll()
        val addresses = identities.map { it.address }

        scope.launch {
            getUtxos(addresses)
        }
    }

    private suspend fun getUtxos(addresses: List<String>) {

        nodeMessenger.send(
            GetUtxosMessage(
                MessageType.UTXOS,
                addresses
            )
        )
    }
}