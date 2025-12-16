package org.szlazakm.node.block

import org.springframework.stereotype.Component
import org.szlazakm.node.domain.Block
import org.szlazakm.node.mempool.MemPool
import org.szlazakm.node.transaction.UTXOService

@Component
class UTXOChainEffectsApplier(
    private val memPool: MemPool,
    private val utxoService: UTXOService
) {

    fun rollback(oldBlocks: List<Block>) {
        val txs = oldBlocks
            .flatMap { it.transactions }
            .filter { !it.isCoinbase() }
        memPool.addTransactions(txs)
    }

    fun apply(newChain: List<Block>) {
        utxoService.rebuildFromChain(newChain)
    }

    fun validateUtxos(chain: List<Block>): Result<Unit> {
        return utxoService.validateTransactionsInChain(chain)
    }
}