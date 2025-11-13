package org.szlazakm.node.domain

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
)