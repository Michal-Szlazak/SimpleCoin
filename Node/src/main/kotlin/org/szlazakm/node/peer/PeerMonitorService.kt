package org.szlazakm.node.peer

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.szlazakm.node.WebSocketClientService
import org.szlazakm.node.config.NodeProperties

@Service
@Slf4j
class PeerMonitorService(
    private val peerService: PeerService,
    private val wsClient: WebSocketClientService,
    private val nodeProperties: NodeProperties
) {

    private val logger = LoggerFactory.getLogger(PeerMonitorService::class.java)

    @Scheduled(initialDelay = 30_000, fixedRate = 15_000)
    @ConditionalOnProperty(name = ["node.ensure-peer-limit"], havingValue = "true", matchIfMissing = false)
    fun ensurePeerLimit() {

        GlobalScope.launch {
            try {
                val activePeers = peerService.getOpenSessionsCount()
                val limit = nodeProperties.peerLimit

                if(activePeers < limit) {

                    val newSessionCandidate = peerService.getPeerWithoutSession()

                    newSessionCandidate?.let {

                        logger.info("Active Peer limit hasn't been reached (active peers: ${activePeers})." +
                                " Trying to connect to new peer (${newSessionCandidate}).")
                        peerService.connectToPeer(newSessionCandidate.value, newSessionCandidate.key)
                    }

                }

            } catch (ex: Exception) {
                logger.error("Error checking or connecting peers", ex)
            }
        }
    }
}