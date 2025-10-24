package org.szlazakm.node.data

data class Block(
    val index: Int,
    val timestamp: Long,
    val previousHash: String,
    val hash: String,
    val data: String,
    val nonce: Long
)

