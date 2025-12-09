package com.szlazakm.wallet.web

import com.szlazakm.wallet.domain.GetBalancesMessage
import com.szlazakm.wallet.domain.MessageType
import com.szlazakm.wallet.domain.TxOutput
import com.szlazakm.wallet.node.ConnectionService
import com.szlazakm.wallet.node.NodeMessenger
import com.szlazakm.wallet.transaction.UTXOService
import com.szlazakm.wallet.wallet.PersistenceService
import com.szlazakm.wallet.wallet.WalletState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/wallet")
class WalletController(
    private val connectionService: ConnectionService,
    private val walletState: WalletState,
    private val utxoService: UTXOService,
    private val nodeMessenger: NodeMessenger,
    private val persistenceService: PersistenceService,
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    @GetMapping
    fun showWallets(): Map<String, Double> {

        return walletState.getWalletBalances()
    }

    @GetMapping("/utxos")
    fun getUtxos(): Map<String, TxOutput> {
        return utxoService.getCurrentUtxos()
    }

}