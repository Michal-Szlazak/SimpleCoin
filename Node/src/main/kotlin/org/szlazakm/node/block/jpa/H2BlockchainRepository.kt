package org.szlazakm.node.block.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface BlockchainRepository: JpaRepository<BlockEntity, Long> {


    @Query("""
    SELECT b FROM BlockEntity b 
    LEFT JOIN FETCH b.transactions
    """)
    fun findAllWithTransactions(): List<BlockEntity>


}
