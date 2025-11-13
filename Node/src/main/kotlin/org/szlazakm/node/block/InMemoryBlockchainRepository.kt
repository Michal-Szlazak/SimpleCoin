package org.szlazakm.node.block

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import org.szlazakm.node.domain.Block

@Repository
class InMemoryBlockchainRepository(
    private val mainChain: MutableList<Block> = mutableListOf()
): BlockchainRepository {

    override fun addBlock(block: Block) { mainChain.add(block) }

    override fun getLast(): Block = mainChain.last()

    override fun getAll(): MutableList<Block> = mainChain

    override fun clear() = mainChain.clear()

    override fun addAll(blocks: List<Block>) { mainChain.addAll(blocks) }

    override fun size() = mainChain.size
}

@Configuration
class InMemoryBlockchainRepositories {

    @Bean
    @Primary
    @Profile("node1")
    fun node1InMemoryBlockchainRepository(): BlockchainRepository {
        return InMemoryBlockchainRepository(
            mutableListOf()
        )
    }

}