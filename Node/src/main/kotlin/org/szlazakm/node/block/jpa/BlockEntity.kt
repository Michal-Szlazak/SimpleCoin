package org.szlazakm.node.block.jpa

import jakarta.persistence.*
import org.szlazakm.node.domain.Block
import org.szlazakm.node.domain.BlockHeader

@Entity
@Table(name = "blocks")
data class BlockEntity(

    @Id
    @Column(nullable = false, unique = true)
    val hash: String,

    @Column(nullable = false)
    val index: Int,

    @Column(nullable = false)
    val timestamp: Long,

    @Column(nullable = false)
    val previousHash: String,

    @Column(nullable = false)
    val nonce: Long,

    @OneToMany(
        mappedBy = "block",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    val transactions: List<TransactionEntity> = emptyList()

) {
    fun toDomain(): Block =
        Block(
            header = BlockHeader(
                index = this.index,
                timestamp = this.timestamp,
                previousHash = this.previousHash,
                hash = this.hash,
                nonce = this.nonce
            ),
            transactions = this.transactions.map { it.toDomain() }
        )

}
