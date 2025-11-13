package org.szlazakm.node.transaction

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.szlazakm.node.domain.Block
import org.szlazakm.node.domain.BlockChainEventNewBlock
import org.szlazakm.node.domain.BlockChainEventRebuilt
import org.szlazakm.node.domain.BlockchainEvent
import org.szlazakm.node.domain.TxOutput
import java.util.concurrent.ConcurrentHashMap

@Service
class UTXOService {

    private val logger = LoggerFactory.getLogger(UTXOService::class.java)
    private val utxos = ConcurrentHashMap<String, TxOutput>()

    @EventListener
    fun onBlockchainEvent(event: BlockchainEvent) {

        logger.info("Received blockchain event: {}", event.type)

        when (event) {
            is BlockChainEventNewBlock -> applyBlock(event.newBlock)
            is BlockChainEventRebuilt -> rebuildFromChain(event.chain)
        }
    }

    private fun applyBlock(block: Block) {
        block.transactions.forEach { tx ->

            tx.inputs.forEach { input ->
                val utxoKey = "${input.txId}:${input.outputIndex}"
                utxos.remove(utxoKey)
            }

            tx.outputs.forEachIndexed { index, output ->
                val utxoKey = "${tx.id}:$index"
                utxos[utxoKey] = output
            }
        }
    }

    private fun rebuildFromChain(blockchain: List<Block>) {
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

    fun getAllUtxos(): Map<String, TxOutput> = utxos.toMap()

    fun getUtxo(txId: String) = utxos[txId]

    fun getUtxos(addresses: List<String>): Map<String, TxOutput> {
        return utxos.filter { (_, value) -> addresses.contains(value.address) }
    }

}