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
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.szlazakm.node.config.NodeProperties
import org.szlazakm.node.peer.PeerService

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(NodeProperties::class)
@EnableScheduling
class NodeApplication(
    private val peerService: PeerService,
    private val nodeProperties: NodeProperties,
) {

    private val logger = LoggerFactory.getLogger(NodeApplication::class.java)

    @Scheduled(fixedRate = 10000)
    fun gossipPeers() {
        GlobalScope.launch {
            peerService.broadcastPeerList()
        }
    }

    @Bean
    fun startup(): CommandLineRunner = CommandLineRunner {

        val nodes = nodeProperties.getStaticNodes()

        logger.info("Loading static peers (${nodes.size})")

        GlobalScope.launch {
            delay(2000)
            nodes.forEach { peerService.connectToPeer(it.value, it.key) }
        }
    }
}

fun main(args: Array<String>) {
	runApplication<NodeApplication>(*args)
}
