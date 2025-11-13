package org.szlazakm.node.web

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.szlazakm.node.block.Blockchain
import org.szlazakm.node.domain.Block
import org.szlazakm.node.domain.BlockHeader

@RestController
@RequestMapping("/block")
class BlockController(
    private val blockchain: Blockchain
) {

    @GetMapping
    fun getCurrentBlockchain(): List<Block> {
        return blockchain.getAllBlocks()
    }
}