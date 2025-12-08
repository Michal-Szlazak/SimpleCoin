package org.szlazakm.node.block

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.szlazakm.node.data.Block
import org.szlazakm.node.peer.message.PeerMessenger

@Service
@ConditionalOnProperty(name = ["miner.enabled"], havingValue = "true", matchIfMissing = false)
class Miner(
    private val blockchain: Blockchain,
    private val blockFactory: BlockFactory,
    private val peerMessenger: PeerMessenger
) {
    private val logger = LoggerFactory.getLogger(Miner::class.java)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var miningJob: Job? = null

    init {
        blockchain.addChainHeadListener { onChainHeadChanged(it) }
    }

    @PostConstruct
    fun init() {
        logger.info("Starting Miner")
        startMining()
    }

    fun startMining() {
        if (miningJob?.isActive == true) return
        miningJob = scope.launch { mineLoop() }
    }

    private fun stopMining() {
        miningJob?.cancel()
        miningJob = null
    }

    private val miningLock = Mutex()

    private fun onChainHeadChanged(newHead: Block) {
        scope.launch {
            val oldJob = miningJob
            miningJob = null
            oldJob?.cancelAndJoin() // wait for old one to finish

            miningLock.withLock {
                logger.info("Chain head changed (#${newHead.index}), restarting mining...")
                startMining()
            }
        }
    }


    private suspend fun mineLoop() {
        while (miningJob?.isActive == true) {
            val lastBlock = blockchain.getLastBlock()
            val data = "Block mined at ${System.currentTimeMillis()}"

            logger.info("Mining on top of #${lastBlock.index}...")

            val newBlock = blockFactory.createBlock(data)

            if (blockchain.getLastBlock().hash != newBlock.previousHash) {
                logger.info("Stale mining result — chain changed while mining, restarting...")
                continue
            }

            val added = blockchain.addBlock(newBlock)

            if (added) {
                logger.info("Mined new block #${newBlock.index}")
                peerMessenger.broadcastNewBlock(newBlock)
            } else {
                logger.warn("Mined block rejected (chain moved?), restarting...")
            }

        }
    }
}
