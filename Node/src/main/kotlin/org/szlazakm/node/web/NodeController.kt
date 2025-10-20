package org.szlazakm.node.web

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.szlazakm.node.peer.PeerRegistry
import org.szlazakm.node.peer.PeerService
import org.szlazakm.node.peer.message.PeerMessenger

@RestController
@RequestMapping("/node")
class NodeController(
    private val peerService: PeerService,
    private val peerRegistry: PeerRegistry,
    private val peerMessenger: PeerMessenger
) {

    private val logger = LoggerFactory.getLogger(NodeController::class.java)

    @GetMapping("/peer")
    fun getPeers() : Map<String, String> {
        return peerRegistry.getPeers()
    }

    @GetMapping("/peer/openSession")
    fun getPeersWithOpenSessions() : Map<String, String> {
        return peerRegistry.getPeersWithOpenSessions()
    }

    @PostMapping("/peer/ping-all")
    suspend fun sendPingToAllPeers() {
        peerMessenger.broadcastPing()
    }

    @PostMapping("/peer/ping")
    suspend fun sendPingToAllPeers(@RequestParam("peer") peer: String) {
        val session = peerRegistry.getSession(peer)

        session?.let {
            peerMessenger.sendPing(session)
        } ?: {
            logger.warn("peer not found or session is null")
        }
    }
}