package org.szlazakm.node.block

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import org.springframework.stereotype.Component
import org.szlazakm.node.config.MinerProperties
import org.szlazakm.node.data.Block
import kotlin.coroutines.coroutineContext

@Component
class BlockFactory(
    private val blockchain: Blockchain,
    minerProperties: MinerProperties,
) {

    private val hashPrefix = "0".repeat(minerProperties.difficulty)

    suspend fun createBlock(data: String): Block {
        val lastBlock = blockchain.getLastBlock()
        val index = lastBlock.index + 1
        val timestamp = System.currentTimeMillis()
        val previousHash = lastBlock.hash

        var nonce = 0L
        var hash: String

        do {
            currentCoroutineContext().ensureActive()
            nonce++
            hash = calculateHash(index, timestamp, previousHash, data, nonce)
        } while (!hash.startsWith(hashPrefix))

        return Block(index, timestamp, previousHash, hash, data, nonce)
    }

    private fun calculateHash(index: Int, timestamp: Long, previousHash: String, data: String, nonce: Long): String {
        val input = "$index$timestamp$previousHash$data$nonce"
        return input.toByteArray().sha256()
    }

    private fun ByteArray.sha256(): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(this)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
