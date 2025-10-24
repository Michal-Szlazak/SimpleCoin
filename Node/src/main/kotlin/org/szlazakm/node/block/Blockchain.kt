package org.szlazakm.node.block

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.szlazakm.node.data.Block
import org.szlazakm.node.data.ChainResponseMessage
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.log

@Service
class Blockchain {

    private val logger = LoggerFactory.getLogger(Blockchain::class.java)

    private val mainChain = mutableListOf<Block>()

    private val chainHeadListeners = CopyOnWriteArrayList<(Block) -> Unit>()

    companion object {
        private const val ALLOWED_FUTURE_DRIFT = 2 * 60 * 1000
    }

    init {
        val genesis = Block(
            index = 0,
            timestamp = 1231006505,
            previousHash = "0",
            hash = "genesis_hash",
            data = "Genesis Block",
            nonce = 0
        )

        val hash = calculateHash(genesis)
        val genesisWithHash = genesis.copy(hash = hash)

        mainChain.add(genesisWithHash)
    }

    fun getLastBlock(): Block = mainChain.last()

    fun getAllBlocks(): List<Block> = mainChain.toList()

    fun addChainHeadListener(listener: (Block) -> Unit) {
        chainHeadListeners.add(listener)
    }

    private fun notifyChainHeadChanged(block: Block) {
        chainHeadListeners.forEach { it.invoke(block) }
    }

    @Synchronized
    fun addBlock(block: Block): Boolean {
        val lastBlock = getLastBlock()

        return when {
            // 1. Extends current main chain

            mainChain.any { it == block } -> {
                logger.info("Received existing block. Skipping.")
                //TODO handle potential fork chains
                false
            }

            block.previousHash == lastBlock.hash && isValidNewBlock(block, lastBlock) -> {
                mainChain.add(block)
                logger.info("Added block #${block.index} to main chain")
                notifyChainHeadChanged(block)
                true
            }

            // 2. Creates a fork (previous hash somewhere in chain)
            mainChain.any { it.hash == block.previousHash && it != block} -> {
                logger.info("Fork detected at block with hash ${block.previousHash}")
                //TODO handle potential fork chains
                false
            }

            else -> {
                logger.warn("Received orphan block (no known parent): ${block.hash}")
                //TODO Resolve the missing parent, request the chain from node
                false
            }
        }
    }

    @Synchronized
    fun resolveChain(blocks: List<Block>) {

        if (validateChain(blocks) && blocks.size > mainChain.size) {
            logger.info("Received chain is longer than current main chain. Replacing main chain. (main: ${mainChain.size}, new: ${blocks.size})")
            mainChain.clear()
            mainChain.addAll(blocks)
        }
    }

    fun validateChain(blocks: List<Block>): Boolean {

        for(i in 1 until blocks.size) {
            if(!isValidBlock(blocks[i], blocks[i - 1])) {
                return false
            }
        }
        return true
    }

    private fun isValidBlock(newBlock: Block, previousBlock: Block): Boolean {
        return previousBlock.index + 1 == newBlock.index &&
                previousBlock.hash == newBlock.previousHash &&
                newBlock.hash == calculateHash(newBlock)
    }

    private fun isValidNewBlock(newBlock: Block, previousBlock: Block): Boolean {
        return previousBlock.index + 1 == newBlock.index &&
                previousBlock.hash == newBlock.previousHash &&
                newBlock.hash == calculateHash(newBlock) &&
                isValidTimestamp(newBlock, previousBlock)
    }

    private fun isValidTimestamp(newBlock: Block, previousBlock: Block): Boolean {
        val now = System.currentTimeMillis()

        //Timestamp must not be too far in the future
        if (newBlock.timestamp > now + ALLOWED_FUTURE_DRIFT) {
            logger.warn("Block timestamp too far in the future: ${newBlock.timestamp - now}ms ahead")
            return false
        }

        //Timestamp must be >= previous block’s timestamp
        if (newBlock.timestamp <= previousBlock.timestamp) {
            logger.warn("Block timestamp is not greater than previous block's timestamp")
            return false
        }

        return true
    }

    private fun calculateHash(block: Block): String {
        val input = "${block.index}${block.timestamp}${block.previousHash}${block.data}${block.nonce}"
        return input.toByteArray().sha256()
    }

    private fun ByteArray.sha256(): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(this)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
