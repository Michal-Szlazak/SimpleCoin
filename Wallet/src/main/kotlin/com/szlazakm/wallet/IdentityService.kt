package com.szlazakm.wallet

import com.szlazakm.wallet.data.Identity
import org.springframework.stereotype.Service
import java.util.Base64

@Service
class IdentityService(
    private val keyService: KeyService,
) {

    fun generateNewIdentity(password: String): Identity {

        val keyPair = keyService.generateKeyPair()
        val encryptionData = keyService.encryptPrivateKey(keyPair.private, password)

        val address = keyService.sha256Ripemd160(keyPair.public.encoded)

        return Identity(
            address = address,
            publicKey = Base64.getEncoder().encodeToString(keyPair.public.encoded),
            encryptedPrivateKey = encryptionData.encryptedPrivateKey,
            salt = encryptionData.salt,
            iv = encryptionData.iv,
        )
    }

}