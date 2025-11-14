package org.szlazakm.node.block.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BlockchainRepository: JpaRepository<BlockEntity, Long> {


//    fun addBlock(block: Block)
//    fun getLast(): Block
//    fun getAll(): MutableList<Block>
//    fun clear()
//    fun addAll(blocks: List<Block>)
//    fun size(): Int

}
