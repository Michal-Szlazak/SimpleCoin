package org.szlazakm.node.block.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.szlazakm.node.domain.TxInput

@Entity
@Table(name = "tx_inputs")
data class TxInputEntity(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val txId: String,  // points to previous TX id

    @Column(nullable = false)
    val outputIndex: Int,

    @Column(nullable = false)
    var sigScript: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    val transaction: TransactionEntity
) {

    constructor() : this(
        id = 0,
        txId = "",
        outputIndex = 0,
        sigScript = "",
        transaction = TransactionEntity()
    )

    fun toDomain() = TxInput(
        txId = txId,
        outputIndex = outputIndex,
        sigScript = sigScript
    )
}
