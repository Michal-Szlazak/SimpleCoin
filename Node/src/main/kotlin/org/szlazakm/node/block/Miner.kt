package org.szlazakm.node.block

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.szlazakm.node.config.MinerProperties
import org.szlazakm.node.domain.Block
import org.szlazakm.node.domain.BlockChainEventNewBlock
import org.szlazakm.node.domain.BlockChainEventRebuilt
import org.szlazakm.node.domain.BlockchainEvent
import org.szlazakm.node.domain.Transaction
import org.szlazakm.node.domain.TxInput
import org.szlazakm.node.domain.TxOutput
import org.szlazakm.node.mempool.MemPool
import org.szlazakm.node.peer.message.PeerMessenger

@Service
class Miner(
    private val blockchain: Blockchain,
    private val blockFactory: BlockFactory,
    private val peerMessenger: PeerMessenger,
    private val memPool: MemPool,
    private val minerProperties: MinerProperties
) {
    private val logger = LoggerFactory.getLogger(Miner::class.java)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var miningJob: Job? = null

    @PostConstruct
    fun init() {
        logger.info("Starting Miner")
        startMining()
    }

    @EventListener
    fun onBlockchainEvent(event: BlockchainEvent) {
        logger.info("Miner notified: ${event.type} event, adjusting strategy.")

        when(event) {
            is BlockChainEventNewBlock -> onChainHeadChanged(event.newBlock)
            is BlockChainEventRebuilt -> onChainHeadChanged(event.chain.last())
        }
    }

    fun startMining() {
        if (miningJob?.isActive == true) return

        if(minerProperties.enabled) {
            miningJob = scope.launch { mineLoop() }
        } else {
            logger.info("Miner disabled. Aborting restart.")
        }
    }

    fun stopMining() {
        scope.launch {
            miningLock.withLock {
                if (miningJob?.isActive == true) {
                    logger.info("Stopping miner...")
                    miningJob?.cancelAndJoin()
                    miningJob = null
                    logger.info("Miner stopped.")
                }
            }
        }
    }

    private val miningLock = Mutex()

    private fun onChainHeadChanged(newHead: Block) {
        scope.launch {
            miningLock.withLock {
                logger.info("Chain head changed (#${newHead.header.index}), restarting mining...")
                miningJob?.cancelAndJoin() // wait for old one to finish
                startMining()
            }
        }
    }

    private suspend fun mineLoop() {
        while (miningJob?.isActive == true) {
            val lastBlock = blockchain.getLastBlock()

            logger.info("Mining on top of #${lastBlock.header.index}...")

            val newBlockHeader = blockFactory.createBlockHeader()

            if (blockchain.getLastBlock().header.hash != newBlockHeader.previousHash) {
                logger.info("Stale mining result — chain changed while mining, restarting...")
                continue
            }

            val poolTransactions = memPool.drainPool(newBlockHeader.hash)
            val transactions = listOf(createRewardTransaction(lastBlock.header.index)) + poolTransactions
            val newBlock = Block(newBlockHeader, transactions)
            val added = blockchain.addBlock(newBlock)

            if (added) {
                logger.info("Mined new block #${newBlockHeader.index}")
                peerMessenger.broadcastNewBlock(newBlock)
            } else {
                logger.warn("Mined block rejected (chain moved?), restarting...")
                memPool.restoreDrainedTransactions(newBlockHeader.hash)
            }

        }
    }

    private fun createRewardTransaction(index: Int): Transaction {

        val coinbaseInput = TxInput(
            txId = "0".repeat(64),
            outputIndex = -1,
            signature = "coinbase_$index"
        )

        val rewardOutput = TxOutput(
            value = minerProperties.reward,
            address = minerProperties.address
        )

        return Transaction(
            id = Transaction.calculateHash(listOf(coinbaseInput), listOf(rewardOutput), minerProperties.publicKey),
            inputs = listOf(coinbaseInput),
            outputs = listOf(rewardOutput),
            publicKey = minerProperties.publicKey,
        )
    }

}
