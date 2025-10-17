package org.szlazakm.node.config

import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

@Component
class NodeHandshakeInterceptor : HandshakeInterceptor {
    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        val nodeId = request.uri.query?.substringAfter("nodeId=")?.substringBefore("&") ?: "unknown"
        attributes["nodeId"] = nodeId
        return true
    }

    override fun afterHandshake(
        request: ServerHttpRequest, response: ServerHttpResponse, wsHandler: WebSocketHandler, ex: Exception?
    ) {
        val nodeId = request.uri.query?.substringAfter("nodeId=")?.substringBefore("&") ?: "unknown"
    }
}