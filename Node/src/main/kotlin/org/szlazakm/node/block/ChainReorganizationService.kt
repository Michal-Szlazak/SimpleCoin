package org.szlazakm.node.block

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.szlazakm.node.domain.Block
import org.szlazakm.node.domain.BlockChainEventNewBlock
import org.szlazakm.node.domain.BlockChainEventRebuilt
import java.util.stream.Collectors

@Component
class ChainReorganizationService(
    private val store: BlockchainStore,
    private val sidechains: SidechainManager,
    private val effects: UTXOChainEffectsApplier,
    private val blockValidator: BlockValidator,
    private val eventPublisher: ApplicationEventPublisher,
) {

    companion object {
        private const val MIN_REORG_DEPTH = 2
    }


    private val logger = LoggerFactory.getLogger(javaClass)

    fun reorganizeIfNeeded(): Boolean {
        val side = sidechains.getLongestSidechain() ?: return false
        if (side.isEmpty()) return false

        val current = store.getChain()

        val forkPoint = current.firstOrNull {
            it.header.hash == side.first().header.previousHash
        } ?: return false

        val forkIndex = current.indexOf(forkPoint)
        val currentDepth = current.size - forkIndex - 1
        val diff = side.size - currentDepth

        when (decideReorg(diff)) {

            ReorgDecision.SKIP -> {
                logger.info("Sidechain below MIN_REORG_DEPTH. Skipping (diff: $diff).")
                return false
            }

            ReorgDecision.REORG -> {
                logger.info("Performing normal reorganization (diff=$diff)")
                return performReorg(current, forkIndex, side)
            }
        }
    }

    private fun performReorg(
        current: List<Block>,
        forkIndex: Int,
        side: List<Block>
    ): Boolean {

        val oldSegment = current.subList(forkIndex + 1, current.size)
        sidechains.saveOldChain(oldSegment)

        effects.rollback(oldSegment)

        val newChain = current.subList(0, forkIndex + 1) + side

        val utxoValid = effects.validateUtxos(newChain)

        if(utxoValid.isFailure) {
            return false
        }

        val chainValid = blockValidator.validateChain(newChain.stream().map { it.header }.collect(Collectors.toList()))

        if(!chainValid) {
            return false
        }

        store.replaceChain(newChain)
        effects.apply(newChain)
        eventPublisher.publishEvent(BlockChainEventRebuilt(chain = store.getChain()))
        return true
    }


    private fun decideReorg(diff: Int): ReorgDecision =
        when {
            diff < MIN_REORG_DEPTH -> ReorgDecision.SKIP
            else -> ReorgDecision.REORG
        }

}

private enum class ReorgDecision {
    SKIP,
    REORG
}
