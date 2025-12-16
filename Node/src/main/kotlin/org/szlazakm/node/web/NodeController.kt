package org.szlazakm.node.web

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.szlazakm.node.block.Miner
import org.szlazakm.node.config.MinerProperties
import org.szlazakm.node.peer.PeerRegistry
import org.szlazakm.node.peer.PeerService
import org.szlazakm.node.peer.message.PeerMessenger

@RestController
@RequestMapping("/node")
class NodeController(
    private val peerRegistry: PeerRegistry,
    private val peerService: PeerService,
    private val peerMessenger: PeerMessenger,
    private val minerProperties: MinerProperties,
    private val miner: Miner
) {

    private val logger = LoggerFactory.getLogger(NodeController::class.java)
    private val scope = CoroutineScope(Dispatchers.IO)

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

    @PostMapping("/peer/connect")
    fun connectToPeer(@RequestParam(value = "nodeId") nodeId: String, @RequestParam(value = "nodeHostname") nodeHostname: String) {
        scope.launch {
            peerService.connectToPeer(
                nodeHostname,
                nodeId
            )
        }
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



    @GetMapping
    fun getNode(model: Model): String {

        model.addAttribute("miner", minerProperties)

        return "node"
    }

    @PostMapping("/miner/disable")
    fun diableMiner(): Unit {
        minerProperties.enabled = false
        miner.stopMining()
    }

    @PostMapping("/miner/enable")
    fun enableMiner(): Unit {
        minerProperties.enabled = true
        miner.startMining()
    }
}