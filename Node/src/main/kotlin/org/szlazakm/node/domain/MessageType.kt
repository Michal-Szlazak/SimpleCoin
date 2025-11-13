package org.szlazakm.node.domain

enum class MessageType {
    PING,
    PONG,
    PEER_LIST,
    NEW_BLOCK,
    REQUEST_CHAIN,
    CHAIN_RESPONSE,
    TRANSACTION,
    GET_BALANCE,
    GET_BALANCES,
    BALANCE,
    BALANCES,
    GET_UTXOS,
    UTXOS
}