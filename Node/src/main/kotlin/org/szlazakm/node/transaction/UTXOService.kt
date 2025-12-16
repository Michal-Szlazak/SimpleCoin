package org.szlazakm.node.transaction

import org.springframework.stereotype.Service
import org.szlazakm.node.domain.Block
import org.szlazakm.node.domain.Transaction
import org.szlazakm.node.domain.TxOutput
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Service
class UTXOService(
) {
    private val utxos = ConcurrentHashMap<String, TxOutput>()
    private val lock = ReentrantLock()

    fun applyBlock(block: Block): Result<Unit> {
        return addTransactionsWithStaging(block.transactions)
    }

    private fun addTransactionsWithStaging(transactions: List<Transaction>): Result<Unit> {
        return addTransactionsWithStaging(utxos, transactions)
    }

    private fun addTransactionsWithStaging(utxos: ConcurrentHashMap<String, TxOutput>, transactions: List<Transaction>): Result<Unit> {

        lock.withLock {

            val stagingUtxos = utxos.toMutableMap()

            transactions.forEachIndexed { index, tx ->

                if(tx.isCoinbase() && index == 0) {
                    tx.outputs.forEachIndexed { index, output ->
                        val utxoKey = "${tx.id}:$index"
                        stagingUtxos[utxoKey] = output
                    }
                } else if(tx.isCoinbase() && index != 0) {
                    return Result.failure(Exception("Wrong coinbase transaction."))
                } else {

                    tx.inputs.forEach { input ->
                        val utxoKey = "${input.txId}:${input.outputIndex}"

                        if(stagingUtxos.containsKey(utxoKey)) {
                            stagingUtxos.remove(utxoKey)
                        } else {
                            return Result.failure(Exception("Transaction with id ${input.txId} not found"))
                        }
                    }

                    tx.outputs.forEachIndexed { index, output ->
                        val utxoKey = "${tx.id}:$index"
                        stagingUtxos[utxoKey] = output
                    }
                }

            }

            utxos.clear()
            utxos.putAll(stagingUtxos)
            return Result.success(Unit)
        }

    }

    fun rebuildFromChain(blockchain: List<Block>) {
        utxos.clear()
        blockchain.forEach { applyBlock(it) }
    }

    fun getBalance(address: String): Double {
        return utxos.values
            .filter { it.address == address }
            .sumOf { it.value }
    }

    fun getAllBalances(addresses: List<String>): Map<String, Double> {
        return utxos.values
            .filter { it.address in addresses }
            .groupBy { it.address }
            .mapValues { (_, outs) -> outs.sumOf { it.value } }
    }

    fun getUtxo(txId: String) = utxos[txId]

    fun getUtxos(addresses: List<String>): Map<String, TxOutput> {
        return utxos.filter { (_, value) -> addresses.contains(value.address) }
    }

    fun validateTransactionsInChain(chain: List<Block>): Result<Unit> {

        val stagingUtxos = ConcurrentHashMap<String, TxOutput>()

        for(block in chain) {
            val result = addTransactionsWithStaging(stagingUtxos, block.transactions)
            if(result.isFailure) {
                return Result.failure(Exception())
            }
        }

        return Result.success(Unit)
    }

}