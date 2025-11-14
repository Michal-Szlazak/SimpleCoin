package org.szlazakm.node.block.jpa

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.szlazakm.node.domain.Transaction
import java.util.Collections.emptyList

@Entity
@Table(name = "transactions")
data class TransactionEntity(

    @Id
    @Column(nullable = false, unique = true)
    val id: String, // transaction hash

    @Column(nullable = false)
    val publicKey: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_hash")
    val block: BlockEntity,

    @OneToMany(
        mappedBy = "transaction",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    val inputs: MutableList<TxInputEntity> = emptyList(),

    @OneToMany(
        mappedBy = "transaction",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    val outputs: MutableList<TxOutputEntity> = emptyList()
) {
    fun toDomain() = Transaction(
        id = id,
        publicKey = publicKey,
        inputs = inputs.map { it.toDomain() },
        outputs = outputs.map { it.toDomain() }
    )
}
