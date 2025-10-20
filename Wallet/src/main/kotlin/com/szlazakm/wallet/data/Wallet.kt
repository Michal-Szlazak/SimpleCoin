package com.szlazakm.wallet.data

data class Identity(
    val address: String,
    val publicKey: String,
    val encryptedPrivateKey: String,
    val salt: String,
    val iv: String
)
