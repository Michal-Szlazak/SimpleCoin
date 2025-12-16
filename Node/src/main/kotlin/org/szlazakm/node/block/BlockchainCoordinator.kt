package org.szlazakm.node.block

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.szlazakm.node.domain.Block
import org.szlazakm.node.domain.BlockChainEventNewBlock
import org.szlazakm.node.domain.MessageType
import org.szlazakm.node.domain.RequestChainMessage
import org.szlazakm.node.peer.message.PeerMessenger

@Component
class BlockchainCoordinator(
    private val store: BlockchainStore,
    private val orphanPool: OrphanPool,
    private val sidechains: SidechainManager,
    private val reorganizer: ChainReorganizationService,
    private val blockValidator: BlockValidator,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val peerMessenger: PeerMessenger
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val logger = LoggerFactory.getLogger(javaClass)

    fun onBlockReceived(block: Block) {
        val last = store.getLastBlock()

        if(block.header.index > last.header.index + 1) {
            scope.launch {
                peerMessenger.broadcast(RequestChainMessage(MessageType.REQUEST_CHAIN))
            }
        }

        val isValidNewBlock = blockValidator.isValidNewBlock(block.header, last.header)


        // 1. Extend main chain
        if (isValidNewBlock && block.header.previousHash == last.header.hash) {

            logger.info("Block added to main chain.")

            store.appendBlock(block)

            applicationEventPublisher.publishEvent(BlockChainEventNewBlock(newBlock = block))

            reorganizer.reorganizeIfNeeded()
            tryAddOrphansToMainChain(orphanPool, store)
            return
        }

        // 2. Sidechain
        val sideChainParentKnown = sidechains.getFlattenedBlocks().any { it.header.hash == block.header.previousHash }
        val currentChainParentKnown = store.getChain().any { it.header.hash == block.header.previousHash }

        if (sideChainParentKnown || currentChainParentKnown) {

            logger.info("Block added to one of the side chains.")
            sidechains.addBlock(block)
            reorganizer.reorganizeIfNeeded()
            tryAddOrphansToSidechains(orphanPool, sidechains)

            return
        }

        // 3. orphans

        logger.info("Block added to orphans pool.")
        orphanPool.add(block)
    }

    fun onChainReceived(chain: List<Block>) {

        val currentChain = store.getChain()

        if(currentChain == chain) {
            logger.info("Foreign chain is identical to current chain. Skipping.")
            return
        }

        val currentIndexByHash = currentChain
            .withIndex()
            .associate { it.value.header.hash to it.index }

        var forkIndexForeign: Int? = null

        for (i in chain.indices.reversed()) {
            val hash = chain[i].header.hash
            val idx = currentIndexByHash[hash]
            if (idx != null) {
                forkIndexForeign = i
                break
            }
        }

        if (forkIndexForeign != null) {

            logger.info("Created subchain from entire chain - received from other node.")

            val subchain = chain.subList(forkIndexForeign + 1, chain.size)
            val forkBlock = currentChain[forkIndexForeign]
            val isFirstBlockValid = blockValidator.isValidBlock(subchain.first().header, forkBlock.header)

            if (isFirstBlockValid) {
                sidechains.addChain(subchain)
            } else {
                logger.warn("First block of forked subchain is not valid. Skipping.")
            }

        } else {
            logger.info("No connection point between foreign chain and current chain.")
        }
    }

    fun tryAddOrphansToMainChain(
        orphanPool: OrphanPool,
        store: BlockchainStore
    ) {
        val last = store.getLastBlock()

        val orphans = orphanPool.getOrphans().toMutableList()
        val resolved = OrphanResolver.resolve(last.header.hash, orphans)

        resolved.forEach {

            val isValid = blockValidator.isValidBlock(
                it.header, store.getLastBlock().header
            )

            if(isValid) {
                store.appendBlock(it)
                applicationEventPublisher.publishEvent(BlockChainEventNewBlock(newBlock = it))
                orphanPool.remove(it)
            } else {
                logger.warn("Orphan block is not valid and wont be added to main chain.")
            }
        }
    }

    fun tryAddOrphansToSidechains(
        orphanPool: OrphanPool,
        sidechainManager: SidechainManager
    ) {
        for (sidechain in sidechainManager.getSidechains()) {
            val tip = sidechain.last().header.hash

            val orphans = orphanPool.getOrphans().toMutableList()
            val resolved = OrphanResolver.resolve(tip, orphans)

            resolved.forEach {

                val firstBlock = sidechain.first()

                val isValid = blockValidator.isValidBlock(
                    it.header, firstBlock.header
                )

                if(isValid) {
                    sidechain.add(it)
                    orphanPool.remove(it)
                } else {
                    logger.warn("Orphan block is not valid and wont be added to side chain.")
                }
            }
        }
    }


}