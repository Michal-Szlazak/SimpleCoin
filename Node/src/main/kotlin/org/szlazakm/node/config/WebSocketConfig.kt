package org.szlazakm.node.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.szlazakm.node.PeerConnectionHandler
import org.szlazakm.node.PeerService

@Configuration
@EnableWebSocket
class WebSocketConfig (private val peerService: PeerService) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(PeerConnectionHandler(peerService), "/ws")
            .setAllowedOrigins("*")
    }
}