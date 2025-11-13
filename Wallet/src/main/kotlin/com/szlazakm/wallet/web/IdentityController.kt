package com.szlazakm.wallet.web

import com.szlazakm.wallet.wallet.IdentityService
import com.szlazakm.wallet.wallet.PersistenceService
import com.szlazakm.wallet.domain.Identity
import com.szlazakm.wallet.domain.IdentityPostDTO
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Controller
@RequestMapping("/identity")
class IdentityController(
    private val identityService: IdentityService,
    private val persistenceService: PersistenceService,
) {

    private val logger = LoggerFactory.getLogger(IdentityController::class.java.name)

    @GetMapping("/new")
    fun showCreateForm(): String {
        return "create_identity"
    }

    @PostMapping("/create")
    fun createIdentity(
        @RequestParam name: String,
        @RequestParam password: String
    ): String {

        val identity = identityService.generateNewIdentity(password)
        val result = persistenceService.persist(name, identity)
        logger.info(result.toString())
        return "redirect:/wallet"
    }
}