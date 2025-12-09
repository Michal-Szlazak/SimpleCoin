package com.szlazakm.wallet.web

import com.szlazakm.wallet.transaction.TransactionService
import com.szlazakm.wallet.domain.Transaction
import com.szlazakm.wallet.domain.TxOutput
import com.szlazakm.wallet.transaction.UTXOService
import com.szlazakm.wallet.wallet.KeyService
import com.szlazakm.wallet.wallet.PersistenceService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/transaction")
class TransactionController(
    private val transactionService: TransactionService,
    private val persistenceService: PersistenceService,
    private val utxoService: UTXOService,
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    @GetMapping("/balance")
    fun getBalance(@RequestParam("walletAddress") walletAddress: String ) {

        scope.launch {
            transactionService.getBalance(walletAddress)
        }
    }

    @GetMapping("/allBalances")
    fun getAllBalances(){

        scope.launch {
            transactionService.getAllBalances()
        }
    }

    @GetMapping("/utxos")
    fun getUtxos() : Map<String, TxOutput> {
        return utxoService.getCurrentUtxos()
    }

    @PostMapping
    fun sendTransaction(@RequestBody transaction: Transaction, identityName: String, password: String) {

        val identity = persistenceService.retrieve(identityName)
        val decryptedPrivateKey = KeyService.decryptPrivateKey(
            identity.encryptedPrivateKey,
            password,
            identity.salt,
            identity.iv
        )

        scope.launch {
            transactionService.sendTransaction(transaction, decryptedPrivateKey, identity.publicKey)
        }
    }
}