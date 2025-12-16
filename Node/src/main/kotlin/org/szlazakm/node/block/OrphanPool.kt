package org.szlazakm.node.block

import org.springframework.stereotype.Component
import org.szlazakm.node.domain.Block
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

@Component
class OrphanPool {

    private val lock = ReentrantReadWriteLock()
    private val orphans = mutableMapOf<String, MutableList<Block>>();

    fun add(block: Block) =
        lock.write {
            orphans.computeIfAbsent(block.header.previousHash) { mutableListOf() }.add(block)
        }

    fun getChildrenOf(parentHash: String): List<Block> =
        lock.read { orphans[parentHash]?.toList() ?: emptyList() }

    fun remove(block: Block) =
        lock.write {
            val list = orphans[block.header.previousHash]
            list?.remove(block)
            if (list?.isEmpty() == true) orphans.remove(block.header.previousHash)
        }

    fun getOrphans(): List<Block> {
        return orphans.flatMap { it.value }
    }
}