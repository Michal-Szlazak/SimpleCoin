package org.szlazakm.node.block

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.szlazakm.node.domain.Block
import org.szlazakm.node.domain.BlockChainEventNewBlock
import org.szlazakm.node.domain.BlockHeader
import org.szlazakm.node.domain.BlockchainEvent
import org.szlazakm.node.domain.BlockchainEventType
import org.szlazakm.node.transaction.TransactionValidator
import java.util.concurrent.CopyOnWriteArrayList

@Service
class Blockchain(
    private val eventPublisher: ApplicationEventPublisher,
    private val transactionValidator: TransactionValidator,
    private val blockchainRepository: BlockchainRepository
) {

    private val logger = LoggerFactory.getLogger(Blockchain::class.java)

    companion object {
        private const val ALLOWED_FUTURE_DRIFT = 2 * 60 * 1000
    }

    init {
        val genesis = BlockHeader(
            index = 0,
            timestamp = 1231006505,
            previousHash = "0",
            hash = "genesis_hash",
            nonce = 0
        )

        val hash = calculateHash(genesis)
        val genesisWithHash = genesis.copy(hash = hash)

        blockchainRepository.addBlock(
            Block(genesisWithHash, emptyList())
        )
    }

    fun getLastBlock(): Block = blockchainRepository.getLast()

    fun getAllBlocks(): List<Block> = blockchainRepository.getAll()

    @Synchronized
    fun addBlock(block: Block): Boolean {

        val validTransactions = transactionValidator.validateTransactions(block.transactions)

        if(validTransactions.isFailure) {
            logger.warn("Transactions from block ${block.header.hash} is invalid. Skipping")
            return false
        }

        val lastBlock = getLastBlock()
        val blockHeader = block.header
        val lastBlockHeader = lastBlock.header

        return when {
            // 1. Extends current main chain

            blockchainRepository.getAll().any { it == block } -> {
                logger.info("Received existing block. Skipping.")
                //TODO handle potential fork chains
                false
            }

            blockHeader.previousHash == lastBlockHeader.hash && isValidNewBlock(blockHeader, lastBlockHeader) -> {
                blockchainRepository.addBlock(block)
                logger.info("Added block #${blockHeader.index} to main chain")

                eventPublisher.publishEvent(
                    BlockChainEventNewBlock(BlockchainEventType.NEW_BLOCK, block)
                )
                true
            }

            // 2. Creates a fork (previous hash somewhere in chain)
            blockchainRepository.getAll().any { it.header.hash == blockHeader.previousHash && it.header != blockHeader} -> {
                logger.info("Fork detected at block with hash ${blockHeader.previousHash}")
                //TODO handle potential fork chains
                false
            }

            else -> {
                logger.warn("Received orphan block (no known parent): ${blockHeader.hash}")
                //TODO Resolve the missing parent, request the chain from node
                false
            }
        }
    }

    @Synchronized
    fun resolveChain(blocks: List<Block>) {

        val blockHeaders = blocks.map { it.header }

        if (validateChain(blockHeaders) && blockHeaders.size > blockchainRepository.size()) {
            logger.info("Received chain is longer than current main chain. Replacing main chain. (main: ${blockchainRepository.size()}, new: ${blockHeaders.size})")
            blockchainRepository.clear()
            blockchainRepository.addAll(blocks)
        }
    }

    fun validateChain(blockHeaders: List<BlockHeader>): Boolean {

        for(i in 1 until blockHeaders.size) {
            if(!isValidBlock(blockHeaders[i], blockHeaders[i - 1])) {
                return false
            }
        }
        return true
    }

    private fun isValidBlock(newBlockHeader: BlockHeader, previousBlockHeader: BlockHeader): Boolean {
        return previousBlockHeader.index + 1 == newBlockHeader.index &&
                previousBlockHeader.hash == newBlockHeader.previousHash &&
                newBlockHeader.hash == calculateHash(newBlockHeader)
    }

    private fun isValidNewBlock(newBlockHeader: BlockHeader, previousBlockHeader: BlockHeader): Boolean {
        return previousBlockHeader.index + 1 == newBlockHeader.index &&
                previousBlockHeader.hash == newBlockHeader.previousHash &&
                newBlockHeader.hash == calculateHash(newBlockHeader) &&
                isValidTimestamp(newBlockHeader, previousBlockHeader)
    }

    private fun isValidTimestamp(newBlockHeader: BlockHeader, previousBlockHeader: BlockHeader): Boolean {
        val now = System.currentTimeMillis()

        //Timestamp must not be too far in the future
        if (newBlockHeader.timestamp > now + ALLOWED_FUTURE_DRIFT) {
            logger.warn("Block timestamp too far in the future: ${newBlockHeader.timestamp - now}ms ahead")
            return false
        }

        //Timestamp must be >= previous block’s timestamp
        if (newBlockHeader.timestamp <= previousBlockHeader.timestamp) {
            logger.warn("Block timestamp is not greater than previous block's timestamp")
            return false
        }

        return true
    }

    private fun calculateHash(blockHeader: BlockHeader): String {
        val input = "${blockHeader.index}${blockHeader.timestamp}${blockHeader.previousHash}${blockHeader.nonce}"
        return input.toByteArray().sha256()
    }

    private fun ByteArray.sha256(): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(this)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
