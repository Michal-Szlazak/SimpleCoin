package org.szlazakm.node.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.szlazakm.node.NodeApplication

@Configuration
@ConfigurationProperties(prefix  = "node")
class NodeProperties {
    var peerLimit: Int = 2
    var nodeId: String = ""
    var staticNodes: List<String> = listOf()

    fun getStaticNodes(): Map<String, String> =
        staticNodes
            .mapNotNull {
                val pair = it.split(",")
                if (pair.size == 2) pair[0].trim() to pair[1].trim() else null
            }
            .toMap()

}