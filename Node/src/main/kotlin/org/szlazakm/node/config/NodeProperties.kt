package org.szlazakm.node.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix  = "node")
class NodeProperties {
    var staticNodes: List<String> = emptyList()
}