package com.szlazakm.wallet.web

import com.szlazakm.wallet.node.ConnectionService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/connection")
class ConnectionController(
    private val connectionService: ConnectionService,
) {

    @PostMapping
    fun connect(host: String) {
        connectionService.connect(host)
    }
}