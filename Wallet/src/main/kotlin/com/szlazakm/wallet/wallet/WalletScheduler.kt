package com.szlazakm.wallet.wallet

import com.szlazakm.wallet.domain.GetBalancesMessage
import com.szlazakm.wallet.domain.MessageType
import com.szlazakm.wallet.node.NodeMessenger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class WalletScheduler(
    private val nodeMessenger: NodeMessenger,
    private val persistenceService: PersistenceService,
    private val walletState: WalletState
) {

    private val scope = CoroutineScope(Dispatchers.Default)

    @Scheduled(fixedRate = 15_000)
    fun start() {

        walletState.refreshWallets()
        val addresses = persistenceService.retrieveAll().map { it.address }

        scope.launch {
            nodeMessenger.send(GetBalancesMessage(MessageType.GET_BALANCES, addresses))
        }
    }

}