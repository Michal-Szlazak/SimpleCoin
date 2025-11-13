package com.szlazakm.wallet.wallet

import com.szlazakm.wallet.domain.Identity
import org.springframework.stereotype.Service
import java.util.Base64

@Service
class IdentityService() {

    fun generateNewIdentity(password: String): Identity {

        val keyPair = KeyService.generateKeyPair()
        val encryptionData = KeyService.encryptPrivateKey(keyPair.private, password)

        val address = KeyService.sha256Ripemd160(keyPair.public.encoded)

        return Identity(
            address = address,
            publicKey = Base64.getEncoder().encodeToString(keyPair.public.encoded),
            encryptedPrivateKey = encryptionData.encryptedPrivateKey,
            salt = encryptionData.salt,
            iv = encryptionData.iv,
        )
    }
}