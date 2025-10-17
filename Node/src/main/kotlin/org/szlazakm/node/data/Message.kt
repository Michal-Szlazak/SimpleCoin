package org.szlazakm.node.data

data class Message(
    val type: String,
    val peers: Map<String, String>? = null,
    val msg: String? = null
)
