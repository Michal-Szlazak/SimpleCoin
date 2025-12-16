package org.szlazakm.node.block

import org.slf4j.LoggerFactory
import org.szlazakm.node.domain.Block

class OrphanResolver {

    companion object {
        private val logger = LoggerFactory.getLogger(OrphanResolver::class.java)

        fun resolve(
            tipHash: String,
            orphans: MutableCollection<Block>
        ): List<Block> {

            val resolved = mutableListOf<Block>()
            val queue = ArrayDeque<String>()
            queue.add(tipHash)

            while (queue.isNotEmpty()) {
                val parentHash = queue.removeFirst()

                val children = orphans
                    .filter { it.header.previousHash == parentHash }

                for (child in children) {
                    resolved.add(child)
                    queue.add(child.header.hash)
                    orphans.remove(child)

                    logger.info("Resolved orphan ${child.header.hash}")
                }
            }

            return resolved
        }
    }
}