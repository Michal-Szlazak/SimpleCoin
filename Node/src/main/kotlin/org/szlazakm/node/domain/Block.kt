package org.szlazakm.node.domain

data class BlockHeader(
    val index: Int,
    val timestamp: Long,
    val previousHash: String,
    val hash: String,
    val nonce: Long
) {
    fun calculateHash(): String {
        val blockHeader = this
        val input = "${blockHeader.index}${blockHeader.timestamp}${blockHeader.previousHash}${blockHeader.nonce}"
        return input.toByteArray().sha256()
    }

    private fun ByteArray.sha256(): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(this)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}

data class Block (
    val header: BlockHeader,
    val transactions: MutableList<Transaction>
)