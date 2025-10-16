package org.szlazakm.node

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.Scheduled
import org.szlazakm.node.config.NodeProperties

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(NodeProperties::class)
class NodeApplication(
    private val wsClient: WebSocketClientService,
    private val peerService: PeerService,
    private val nodeProperties: NodeProperties
) {

    private val logger = LoggerFactory.getLogger(NodeApplication::class.java)

    @Scheduled(fixedRate = 10000)
    fun gossipPeers() {
        GlobalScope.launch { peerService.broadcastPeerList() }
    }

    @Bean
    fun startup(): CommandLineRunner = CommandLineRunner {

        logger.info("Loading static peers (${nodeProperties.staticNodes.size})")

        GlobalScope.launch {
            delay(2000)
            nodeProperties.staticNodes.forEach { wsClient.connectToPeer(it) }
        }
    }
}

fun main(args: Array<String>) {
	runApplication<NodeApplication>(*args)
}
