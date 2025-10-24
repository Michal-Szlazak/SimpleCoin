package org.szlazakm.node.data

enum class MessageType {
    PING,
    PONG,
    PEER_LIST,
    NEW_BLOCK,
    REQUEST_CHAIN,
    CHAIN_RESPONSE
}