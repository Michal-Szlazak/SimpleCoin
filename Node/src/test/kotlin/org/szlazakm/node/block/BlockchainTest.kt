package org.szlazakm.node.block

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.szlazakm.node.data.Block
import java.util.concurrent.atomic.AtomicReference

class BlockchainTest {

    private lateinit var blockchain: Blockchain

    @BeforeEach
    fun setup() {
        blockchain = Blockchain()
    }

    private fun makeValidBlock(prev: Block, data: String = "data"): Block {
        val index = prev.index + 1
        val timestamp = System.currentTimeMillis()
        val previousHash = prev.hash
        val nonce = 1L
        val input = "$index$timestamp$previousHash$data$nonce"
        val hash = input.toByteArray().sha256()
        return Block(index, timestamp, previousHash, hash, data, nonce)
    }

    private fun ByteArray.sha256(): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(this)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    @Test
    fun `genesis block should exist after init`() {
        val blocks = blockchain.getAllBlocks()
        assertEquals(1, blocks.size)
        assertEquals("Genesis Block", blocks.first().data)
    }

    @Test
    fun `valid block should be added to main chain`() {
        val prev = blockchain.getLastBlock()
        val newBlock = makeValidBlock(prev)
        val added = blockchain.addBlock(newBlock)

        assertTrue(added)
        assertEquals(2, blockchain.getAllBlocks().size)
        assertEquals(newBlock.hash, blockchain.getLastBlock().hash)
    }

    @Test
    fun `block with wrong previous hash should be rejected`() {
        val prev = blockchain.getLastBlock()
        val invalidBlock = makeValidBlock(prev).copy(previousHash = "wrong-hash")

        val added = blockchain.addBlock(invalidBlock)
        assertFalse(added)
        assertEquals(1, blockchain.getAllBlocks().size)
    }

    @Test
    fun `block with invalid timestamp should be rejected`() {
        val prev = blockchain.getLastBlock()
        val badTimestamp = System.currentTimeMillis() + 10_000_000
        val invalidBlock = makeValidBlock(prev).copy(timestamp = badTimestamp)

        val added = blockchain.addBlock(invalidBlock)
        assertFalse(added)
        assertEquals(1, blockchain.getAllBlocks().size)
    }

    @Test
    fun `should trigger chain head listener when new block added`() {
        val captured = AtomicReference<Block?>(null)
        blockchain.addChainHeadListener { captured.set(it) }

        val prev = blockchain.getLastBlock()
        val block = makeValidBlock(prev)
        blockchain.addBlock(block)

        assertEquals(block, captured.get())
    }
}
