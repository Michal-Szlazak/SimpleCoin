package org.szlazakm.node.wallet

import org.springframework.stereotype.Service
import org.szlazakm.node.transaction.UTXOService

@Service
class WalletService(
    private val utxoService: UTXOService
) {

    fun getBalance(address: String): Double {
        return utxoService.getBalance(address)
    }

    fun getAllBalances(addresses: List<String>): Map<String, Double> {
        return utxoService.getAllBalances(addresses)
    }
}