package org.szlazakm.node.data

data class Message(
    val type: String,
    val peers: List<String>? = null,
    val msg: String? = null
)
