package org.szlazakm.node.domain

import org.szlazakm.node.block.jpa.BlockEntity

data class BlockHeader(
    val index: Int,
    val timestamp: Long,
    val previousHash: String,
    val hash: String,
    val nonce: Long
)

data class Block (
    val header: BlockHeader,
    val transactions: List<Transaction>
) {
    fun toEntity(): BlockEntity {
        val blockEntity = BlockEntity(
            hash = header.hash,
            index = header.index,
            timestamp = header.timestamp,
            previousHash = header.previousHash,
            nonce = header.nonce,
            transactions = mutableListOf() // added after creation
        )

        val txEntities = transactions.map { it.toEntity(blockEntity) }

        return blockEntity.copy(transactions = txEntities.toMutableList())
    }
}