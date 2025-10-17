package org.szlazakm.node.web

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.szlazakm.node.peer.PeerService

@RestController
@RequestMapping("/node")
class NodeController(
    private val peerService: PeerService,
) {

    @GetMapping("/peer")
    fun getPeers() : Map<String, String> {
        return peerService.getPeers()
    }

    @GetMapping("/peer/openSession")
    fun getPeersWithOpenSessions() : Map<String, String> {
        return peerService.getPeersWithOpenSessions()
    }

    @PostMapping("/peer/ping-all")
    suspend fun sendPingToAllPeers() {
        peerService.broadcastPing()
    }


}