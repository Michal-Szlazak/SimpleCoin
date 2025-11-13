package com.szlazakm.wallet.domain

open class Identity(
    open val address: String,
    open val publicKey: String,
    open val encryptedPrivateKey: String,
    open val salt: String,
    open val iv: String
)

data class NamedIdentity(
    val name: String,
    override val address: String,
    override val publicKey: String,
    override val encryptedPrivateKey: String,
    override val salt: String,
    override val iv: String
) : Identity(address, publicKey, encryptedPrivateKey, salt, iv) {

    constructor(identity: Identity, name: String) : this(
        name, identity.address, identity.publicKey, identity.encryptedPrivateKey, identity.salt, identity.iv
    )
}

