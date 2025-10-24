package org.szlazakm.node.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix  = "miner")
class MinerProperties {
    var enabled: Boolean = false
    var difficulty: Int = 0
}