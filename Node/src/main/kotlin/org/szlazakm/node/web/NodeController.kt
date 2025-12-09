package org.szlazakm.node.web

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
import org.szlazakm.node.peer.message.PeerMessenger

@RestController
@RequestMapping("/node")
class NodeController(
    private val peerRegistry: PeerRegistry,
    private val peerMessenger: PeerMessenger,
    private val minerProperties: MinerProperties,
    private val miner: Miner
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

    @GetMapping
    fun getNode(model: Model): String {

        model.addAttribute("miner", minerProperties)

        return "node"
    }

    @PostMapping("/miner/update")
    fun updateMiner(@ModelAttribute minerForm: MinerProperties, model: Model): String {
        minerProperties.enabled = minerForm.enabled
        minerProperties.difficulty = minerForm.difficulty
        minerProperties.address = minerForm.address
        minerProperties.publicKey = minerForm.publicKey

        if (minerForm.enabled) miner.startMining()
        else miner.stopMining()

        model.addAttribute("miner", minerProperties)
        model.addAttribute("message", "Miner configuration updated successfully.")
        return "redirect:/node"
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