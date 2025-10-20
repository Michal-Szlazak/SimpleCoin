package org.szlazakm.node.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.szlazakm.node.NodeApplication

@Configuration
@ConfigurationProperties(prefix  = "node")
class NodeProperties {
    var peerLimit: Int = 2
    var nodeId: String = ""
    var staticNodes: String = ""
    var nodePort: String = ""
    var nodeHostname: String = ""

    fun getStaticNodes(): Map<String, String> {

        val listOfPairs = staticNodes.split(";")

        return listOfPairs.mapNotNull {
                val pair = it.split(",")
                if (pair.size == 2) pair[0].trim() to pair[1].trim() else null
            }
            .toMap()
    }

}