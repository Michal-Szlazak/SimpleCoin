package com.szlazakm.wallet.transaction

import com.szlazakm.wallet.domain.TxOutput
import org.springframework.stereotype.Service

@Service
class UTXOService() {

    private val utxos = mutableMapOf<String, TxOutput>()

    fun getCurrentUtxos(): Map<String, TxOutput> {
        return utxos
    }

    fun getCurrentUtxos(txId: String): TxOutput? {
        return utxos[txId]
    }

    fun updateCurrentUtxos(utxos: Map<String, TxOutput>) {
        this.utxos.clear()
        this.utxos.putAll(utxos)
    }
}