package org.szlazakm.node.domain

sealed class BlockchainEvent {
    abstract val type: BlockchainEventType
}

data class BlockChainEventNewBlock(
    override val type: BlockchainEventType = BlockchainEventType.NEW_BLOCK,
    val newBlock: Block
): BlockchainEvent()

data class BlockChainEventRebuilt(
    override val type: BlockchainEventType = BlockchainEventType.REBUILT,
    val chain: List<Block>
): BlockchainEvent()

enum class BlockchainEventType {
    NEW_BLOCK,
    REBUILT,
}
