package com.szlazakm.wallet.web

import com.szlazakm.wallet.IdentityService
import com.szlazakm.wallet.KeyService
import com.szlazakm.wallet.PersistenceService
import com.szlazakm.wallet.data.Identity
import com.szlazakm.wallet.data.IdentityPostDTO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.Base64

@RestController
@RequestMapping("/identity")
class IdentityController(
    private val identityService: IdentityService,
    private val persistenceService: PersistenceService,
    private val keyService: KeyService,
) {

    @PostMapping()
    fun createIdentity(@RequestBody dto: IdentityPostDTO) {

        val identity = identityService.generateNewIdentity(dto.password)
        persistenceService.persist(dto.name, identity)
    }

    @GetMapping()
    fun getIdentity(@RequestParam name: String): Identity {
        return persistenceService.retrieve(name)
    }

    @GetMapping("/all")
    fun getAllIdentities(): List<Identity> {
        return persistenceService.retrieveAll()
    }

    @GetMapping("/decryptedKey")
    fun getDecryptedKey(@RequestParam name: String, @RequestParam password: String): String {

        val identity = persistenceService.retrieve(name)
        val keyBytes = keyService.decryptPrivateKey(
            identity.encryptedPrivateKey,
            password,
            identity.salt,
            identity.iv
        )
        return Base64.getEncoder().encodeToString(keyBytes)
    }
}