package org.szlazakm.node.block

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.szlazakm.node.domain.Block
import org.szlazakm.node.transaction.TransactionValidator

@Service
class Blockchain(
    private val transactionValidator: TransactionValidator,
    private val coordinator: BlockchainCoordinator,
    private val store: BlockchainStore
) {

    private val logger = LoggerFactory.getLogger(Blockchain::class.java)

    fun getLastBlock(): Block? = store.getLastBlock()

    fun getAllBlocks(): List<Block> = store.getChain()

    @Synchronized
    fun addBlock(block: Block): Boolean {

        // Stateless validation only
        val txValidation = transactionValidator.validateTransactions(block.transactions)
        if (txValidation.isFailure) {
            logger.warn("Invalid block transactions: ${block.header.hash}")
            return false
        }

        coordinator.onBlockReceived(block)

        return true
    }

    fun resolveIncomingChain(chain: List<Block>) {

        coordinator.onChainReceived(chain)

    }
}
