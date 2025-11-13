package org.szlazakm.node.peer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.szlazakm.node.config.NodeProperties

@Service
class PeerMonitorService(
    private val peerService: PeerService,
    private val peerRegistry: PeerRegistry,
    private val nodeProperties: NodeProperties
) {

    private val logger = LoggerFactory.getLogger(PeerMonitorService::class.java)

    @Scheduled(initialDelay = 30_000, fixedRate = 15_000)
    @ConditionalOnProperty(name = ["node.ensure-peer-limit"], havingValue = "true", matchIfMissing = false)
    fun ensurePeerLimit() = CoroutineScope(Dispatchers.Default).launch {
            try {
                val activePeers = peerRegistry.getOpenSessionsCount()
                val limit = nodeProperties.peerLimit

                if(activePeers < limit) {

                    val newSessionCandidate = peerRegistry.getPeerWithoutSession()

                    newSessionCandidate?.let {

                        logger.debug(
                            "Active Peer limit hasn't been reached (active peers: {}). Trying to connect to new peer ({}).",
                            activePeers,
                            newSessionCandidate
                        )
                        peerService.connectToPeer(newSessionCandidate.value, newSessionCandidate.key)
                    }

                }

            } catch (ex: Exception) {
                logger.error("Error checking or connecting peers", ex)
            }
    }
}