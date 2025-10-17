package org.szlazakm.node.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.szlazakm.node.peer.PeerConnectionHandler
import org.szlazakm.node.peer.PeerService

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val peerConnectionHandler: PeerConnectionHandler,
    private val nodeHandshakeInterceptor: NodeHandshakeInterceptor
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(peerConnectionHandler, "/ws")
            .addInterceptors(nodeHandshakeInterceptor)
            .setAllowedOrigins("*")
    }
}