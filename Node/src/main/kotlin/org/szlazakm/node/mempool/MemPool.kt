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


    fun addTransactions(transactions: List<Transaction>) {
        transactions.forEach { addTransaction(it)}
    }

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

    fun getAll(): List<Transaction> = pool.toList()

    fun clear() = pool.clear()

}