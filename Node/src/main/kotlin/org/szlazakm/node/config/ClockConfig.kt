package org.szlazakm.node.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.InstantSource

@Configuration
class ClockConfig {

    @Bean
    fun instantSource(): InstantSource = InstantSource.system()
}