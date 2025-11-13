package org.szlazakm.node.block

import org.springframework.stereotype.Component
import org.szlazakm.node.config.MinerProperties
import org.szlazakm.node.domain.BlockHeader
import java.time.InstantSource

@Component
class BlockFactory(
    private val blockchain: Blockchain,
    minerProperties: MinerProperties,
    private val instantSource: InstantSource
) {

    private val hashPrefix = "0".repeat(minerProperties.difficulty)

    suspend fun createBlockHeader(): BlockHeader {
        val lastBlock = blockchain.getLastBlock()
        val index = lastBlock.header.index + 1
        val timestamp = instantSource.instant().toEpochMilli()
        val previousHash = lastBlock.header.hash

        var nonce = 0L
        var hash: String

        do {
            nonce++
            hash = calculateHash(index, timestamp, previousHash, nonce)
        } while (!hash.startsWith(hashPrefix))

        return BlockHeader(index, timestamp, previousHash, hash, nonce)
    }

    private fun calculateHash(index: Int, timestamp: Long, previousHash: String, nonce: Long): String {
        val input = "$index$timestamp$previousHash$nonce"
        return input.toByteArray().sha256()
    }

    private fun ByteArray.sha256(): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(this)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
