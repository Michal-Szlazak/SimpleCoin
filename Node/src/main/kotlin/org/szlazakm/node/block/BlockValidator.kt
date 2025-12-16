package org.szlazakm.node.block

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.szlazakm.node.config.MinerProperties
import org.szlazakm.node.domain.BlockHeader

@Service
class BlockValidator(
    private val minerProperties: MinerProperties,

) {

    companion object {
        private const val ALLOWED_FUTURE_DRIFT = 2 * 60 * 1000
    }

    private val logger = LoggerFactory.getLogger(BlockValidator::class.java)

    fun validateChainFromGenesis(blockHeaders: List<BlockHeader>): Boolean {

        if (blockHeaders.isEmpty() || blockHeaders[0] != GenesisBlockHeader.header) {
            return false
        }

        return validateChain(blockHeaders)
    }

    fun validateChain(blockHeaders: List<BlockHeader>): Boolean {

        for(i in 1 until blockHeaders.size) {
            if(!isValidBlock(blockHeaders[i], blockHeaders[i - 1])) {
                return false
            }
        }
        return true
    }

     fun isValidBlock(newBlockHeader: BlockHeader, previousBlockHeader: BlockHeader): Boolean {
        return previousBlockHeader.index + 1 == newBlockHeader.index &&
                previousBlockHeader.hash == newBlockHeader.previousHash &&
                newBlockHeader.hash == calculateHash(newBlockHeader) &&
                newBlockHeader.hash.startsWith("0".repeat(minerProperties.difficulty))
    }

     fun isValidNewBlock(newBlockHeader: BlockHeader, previousBlockHeader: BlockHeader): Boolean {
        return previousBlockHeader.index + 1 == newBlockHeader.index &&
                previousBlockHeader.hash == newBlockHeader.previousHash &&
                newBlockHeader.hash == calculateHash(newBlockHeader) &&
                isValidTimestamp(newBlockHeader, previousBlockHeader) &&
                newBlockHeader.hash.startsWith("0".repeat(minerProperties.difficulty))
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