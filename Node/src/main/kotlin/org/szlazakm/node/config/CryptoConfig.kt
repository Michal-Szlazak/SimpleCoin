package org.szlazakm.node.config

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.springframework.context.annotation.Configuration
import java.security.Security

@Configuration
class CryptoConfig {
    init {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }
}