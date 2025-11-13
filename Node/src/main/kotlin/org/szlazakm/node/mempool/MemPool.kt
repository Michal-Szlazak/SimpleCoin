package org.szlazakm.node.mempool

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.szlazakm.node.domain.Transaction
import org.szlazakm.node.transaction.TransactionValidator
import java.util.Collections

@Service
class MemPool(
    private val transactionValidator: TransactionValidator
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val pool: MutableList<Transaction> = Collections.synchronizedList(mutableListOf<Transaction>())
    private val drainedPool: MutableMap<String, List<Transaction>> = Collections.synchronizedMap(mutableMapOf())

    fun addTransaction(transaction: Transaction): Result<Unit> {

        val validInputs = transactionValidator.validateTransaction(transaction)

        return if(validInputs.isSuccess) {
            logger.info("Adding transaction ${transaction.id} to pool.")
            pool.add(transaction)
            Result.success(Unit)
        } else {
            logger.warn("Skipping transaction ${transaction.id} due to invalid inputs.")
             Result.failure(Exception("Skipping transaction ${transaction.id} due to invalid inputs."))
        }
    }

    fun drainPool(blockHash: String): List<Transaction> {
        synchronized(pool) {
            val currentList = pool.toList()
            pool.clear()
            drainedPool[blockHash] = currentList
            return currentList
        }
    }

    fun restoreDrainedTransactions(blockHash: String) {

        val drainedTransactions = drainedPool[blockHash]
        drainedTransactions?.let {
            pool.addAll(it)
        } ?: {
            logger.warn("No drained transactions found for hash $blockHash. Transactions not restored.")
        }
    }

}