package org.szlazakm.node.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.szlazakm.node.peer.PeerConnectionHandler
import org.szlazakm.node.wallet.WalletConnectionHandler

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val peerConnectionHandler: PeerConnectionHandler,
    private val walletConnectionHandler: WalletConnectionHandler,
    private val nodeHandshakeInterceptor: NodeHandshakeInterceptor
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(peerConnectionHandler, "/ws/node")
            .addHandler(walletConnectionHandler, "/ws/wallet")
            .addInterceptors(nodeHandshakeInterceptor)
            .setAllowedOrigins("*")
    }
}