package org.szlazakm.node

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.szlazakm.node.config.NodeProperties
import org.szlazakm.node.domain.MessageType
import org.szlazakm.node.domain.RequestChainMessage
import org.szlazakm.node.peer.PeerService
import org.szlazakm.node.peer.message.PeerMessenger

@SpringBootApplication
@EnableConfigurationProperties(NodeProperties::class)
@EnableScheduling
class NodeApplication(
    private val peerService: PeerService,
    private val peerMessenger: PeerMessenger,
    private val nodeProperties: NodeProperties
) {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val logger = LoggerFactory.getLogger(NodeApplication::class.java)

//    @Scheduled(initialDelay = 30_000, fixedRate = 10_000)
//    @ConditionalOnProperty(name = ["node.gossip-peers"], havingValue = "true", matchIfMissing = false)
//    fun gossipPeers() {
//        logger.info("Starting gossip peers")
//        coroutineScope.launch {
//            try {
//                peerMessenger.broadcastPeerList()
//            } catch (e: Exception) {
//                logger.error("Error broadcasting peer list", e)
//            }
//        }
//    }

    @Bean
    fun startup(): CommandLineRunner = CommandLineRunner {
        val nodes = nodeProperties.getStaticNodes()
        logger.info("Loading static peers (${nodes.size})")

        coroutineScope.launch {
            delay(2000)
            nodes.forEach { (nodeId, host) ->
                try {
                    peerService.connectToPeer(host, nodeId)
                } catch (e: Exception) {
                    logger.error("Failed to connect to peer $nodeId at $host", e)
                }
            }
            peerMessenger.broadcast(RequestChainMessage(MessageType.REQUEST_CHAIN))
        }
    }
}

fun main(args: Array<String>) {
	runApplication<NodeApplication>(*args)
}
