package org.szlazakm.node.block

import org.springframework.stereotype.Component
import org.szlazakm.node.domain.Block
import org.szlazakm.node.domain.BlockHeader
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

@Component
class BlockchainStore(
    private val blockValidator: BlockValidator
) {

    private val lock = ReentrantReadWriteLock()
    private val chain = mutableListOf<Block>()

    init {
        // Initialize genesis
        val genesisHeader = BlockHeader(
            index = 0,
            timestamp = 1231006505,
            previousHash = "0",
            hash = "",
            nonce = 0
        )

        val hash = genesisHeader.calculateHash()
        val genesisWithHash = genesisHeader.copy(hash = hash)

        chain.add(Block(genesisWithHash, mutableListOf()))
    }

    fun getChain(): List<Block> =
        lock.read { chain.toList() }

    fun getLastBlock(): Block =
        lock.read { chain.last() }

    fun appendBlock(block: Block): Result<Unit> =
        lock.write {

            val lastBlock = chain.last()
            if(blockValidator.isValidNewBlock(block.header, lastBlock.header)) {
                chain.add(block)
                return Result.success(Unit)
            }

            return Result.failure(Exception("Invalid new block."))
        }

    fun replaceChain(newChain: List<Block>) =
        lock.write {
            chain.clear()
            chain.addAll(newChain)
        }
}