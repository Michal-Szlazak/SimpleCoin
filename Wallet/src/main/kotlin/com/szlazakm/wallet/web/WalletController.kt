package com.szlazakm.wallet.web

import com.szlazakm.wallet.domain.TxOutput
import com.szlazakm.wallet.node.ConnectionService
import com.szlazakm.wallet.transaction.UTXOService
import com.szlazakm.wallet.wallet.IdentityService
import com.szlazakm.wallet.wallet.PersistenceService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/wallet")
class WalletController(
    private val connectionService: ConnectionService,
    private val persistenceService: PersistenceService,
    private val utxoService: UTXOService
) {

    @GetMapping
    fun showWallet(model: Model): String {

        model.addAttribute("identities", persistenceService.retrieveAll())
        model.addAttribute("utxos", utxoService.getCurrentUtxos())

        // Connection status
        val session = connectionService.currentSession
        val connected = session != null && session.isOpen
        model.addAttribute("connected", connected)

        return "wallet"
    }

    @GetMapping("/utxos")
    @ResponseBody
    fun getUtxos(): Map<String, TxOutput> {
        return utxoService.getCurrentUtxos()
    }

    @PostMapping("/node/connect")
    fun nodeConnect(
        @RequestParam(value = "host") nodeHost: String,
    ): String {
        connectionService.connect(nodeHost)
        return "redirect:/wallet"
    }

}