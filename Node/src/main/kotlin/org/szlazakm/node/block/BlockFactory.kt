package org.szlazakm.node.block

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.szlazakm.node.config.MinerProperties
import org.szlazakm.node.domain.Block
import org.szlazakm.node.domain.BlockHeader
import java.time.InstantSource

@Component
class BlockFactory (
    private val blockchain: Blockchain,
    minerProperties: MinerProperties,
    private val instantSource: InstantSource
){

    companion object {

        fun genesisBlock(): BlockHeader {

            val genesis = BlockHeader(
                index = 0,
                timestamp = 1231006505,
                previousHash = "0",
                hash = "genesis_hash",
                nonce = 0
            )

            val hash = genesis.calculateHash()
            val genesisWithHash = genesis.copy(hash = hash)

            return genesisWithHash
        }


    }

    private val hashPrefix = "0".repeat(minerProperties.difficulty)

    fun createBlockHeader(lastBlock: Block): BlockHeader {

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
