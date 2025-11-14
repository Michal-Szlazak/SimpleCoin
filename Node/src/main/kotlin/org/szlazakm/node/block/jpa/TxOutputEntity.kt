package org.szlazakm.node.block.jpa;

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.szlazakm.node.domain.TxOutput

@Entity
@Table(name = "tx_outputs")
data class TxOutputEntity(

        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = 0,

        @Column(nullable = false)
        val value: Double,

        @Column(nullable = false)
        val address: String,

        @Column(nullable = false)
        val outputIndex: Int, // needed for UTXO references

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "transaction_id")
        val transaction: TransactionEntity
) {
    fun toDomain() = TxOutput(
        value = value,
        address = address
    )
}

