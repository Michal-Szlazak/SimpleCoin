package org.szlazakm.node.block

import org.szlazakm.node.domain.Block

interface BlockchainRepository {

    fun addBlock(block: Block)
    fun getLast(): Block
    fun getAll(): MutableList<Block>
    fun clear()
    fun addAll(blocks: List<Block>)
    fun size(): Int
}