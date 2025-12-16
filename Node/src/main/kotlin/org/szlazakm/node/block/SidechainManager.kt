package org.szlazakm.node.block

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.szlazakm.node.domain.Block
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.stream.Collectors
import kotlin.concurrent.read
import kotlin.concurrent.write

@Component
class SidechainManager(
    private val blockValidator: BlockValidator
) {

    private val lock = ReentrantReadWriteLock()
    private val sidechains = mutableMapOf<String, MutableList<Block>>()
    private val logger = LoggerFactory.getLogger(SidechainManager::class.java)

    fun getSidechains(): List<MutableList<Block>> {
        return sidechains.values.toList()
    }

    fun addBlock(block: Block) =
        lock.write {

            val allBlocks = getFlattenedBlocks()

            if(allBlocks.contains(block)) {
                logger.info("Block ${block.header.hash} already exists inside sidechains. Skipping.")
                return
            }

            sidechains.forEach {
                val lastBlock = it.value.last()
                if(lastBlock.header.hash == block.header.previousHash) {

                    if(blockValidator.isValidBlock(block.header, lastBlock.header)) {
                        it.value.add(block)
                        logger.info("Block ${block.header.previousHash} added to sidechain.")
                        return
                    } else {
                        logger.warn("Block ${block.header.hash} is not valid and wont be added to side chain. Skipping.")
                    }

                }
            }

            sidechains[block.header.previousHash] = mutableListOf(block)
        }

    fun getFlattenedBlocks(): List<Block> = lock.read { sidechains.values.flatten() }

    fun getLongestSidechain(): List<Block>? =
        lock.read {
            sidechains.values.maxByOrNull { it.size }?.toList()
        }

    fun saveOldChain(chainSegment: List<Block>) =
        lock.write {
            if (chainSegment.isNotEmpty()) {
                val parent = chainSegment.first().header.previousHash
                sidechains[parent] = chainSegment.toMutableList()
            }
        }

    fun addChain(chain: List<Block>) {

        val validChain = blockValidator.validateChain(chain.stream().map { it.header }.collect(Collectors.toList()))

        if(!validChain) {
            logger.warn("Chain ${chain.first().header.hash} is not a valid subchain. Skipping.")
            return
        }

        sidechains[chain.first().header.hash] = chain.toMutableList()
    }

}