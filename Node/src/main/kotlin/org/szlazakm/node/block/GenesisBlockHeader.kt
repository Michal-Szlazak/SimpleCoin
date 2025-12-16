package org.szlazakm.node.block

import org.szlazakm.node.domain.BlockHeader

object GenesisBlockHeader {

    val header: BlockHeader by lazy {
        val genesis = BlockHeader(
            index = 0,
            timestamp = 1231006505,
            previousHash = "0",
            hash = "genesis_hash",
            nonce = 0
        )

        genesis.copy(hash = genesis.calculateHash())
    }
}