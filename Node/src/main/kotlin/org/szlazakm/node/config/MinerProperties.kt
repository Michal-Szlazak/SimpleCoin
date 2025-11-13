package org.szlazakm.node.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix  = "miner")
class MinerProperties {
    var enabled: Boolean = false
    var difficulty: Int = 0
    var reward: Double = 0.0
    var address: String = ""
    var publicKey: String = ""
}