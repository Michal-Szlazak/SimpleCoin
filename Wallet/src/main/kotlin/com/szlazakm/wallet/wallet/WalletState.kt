package com.szlazakm.wallet.wallet

import com.szlazakm.wallet.domain.GetBalancesMessage
import com.szlazakm.wallet.domain.MessageType
import com.szlazakm.wallet.node.NodeMessenger
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
class WalletState(
    val persistenceService: PersistenceService
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    @PostConstruct
    fun init() {
        refreshWallets()
    }

    fun getWalletBalances(): MutableMap<String, Double> {
        return walletBalances
    }

    private val walletBalances = mutableMapOf<String, Double>()

    fun refreshWallets() {

        val namedIdentities = persistenceService.retrieveAll()

        namedIdentities.forEach { identity ->
            if(!walletBalances.containsKey(identity.address)) {
                walletBalances[identity.address] = 0.0
            }
        }
    }

    fun updateBalance(walletAddress: String, walletBalance: Double) {
        walletBalances[walletAddress] = walletBalance
    }

    fun updateBalances(map: Map<String, Double>) {
        walletBalances.putAll(map)
    }

}
