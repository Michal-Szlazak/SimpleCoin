package org.szlazakm.node.block

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.szlazakm.node.config.MinerProperties
import org.szlazakm.node.domain.Block
import org.szlazakm.node.domain.BlockHeader
import java.time.InstantSource
import java.time.LocalDateTime
import java.time.ZoneOffset

class BlockFactoryTest {

    private lateinit var blockchain: Blockchain
    private lateinit var blockFactory: BlockFactory
    private lateinit var minerProperties: MinerProperties
    private lateinit var instantSource: InstantSource

    @BeforeEach
    fun setup() {
        blockchain = mockk()
        minerProperties = mockk()
        instantSource = mockk()
    }

    @Test
    fun `should create a valid block header with correct difficulty`() = runTest {
        // given
        val difficulty = 5
        every { minerProperties.difficulty } returns difficulty
        every { instantSource.instant() } returns InstantSource.system().instant()

        val lastBlockHeader = BlockHeader(
            index = 0,
            timestamp = 123456789L,
            previousHash = "0",
            hash = "abcd",
            nonce = 1
        )
        val lastBlock = Block(header = lastBlockHeader, transactions = emptyList())

        every { blockchain.getLastBlock() } returns lastBlock

        blockFactory = BlockFactory(blockchain, minerProperties, instantSource)

        // when
        val newHeader = blockFactory.createBlockHeader()

        // then
        assertThat(newHeader.index).isEqualTo(1)
        assertThat(newHeader.previousHash).isEqualTo(lastBlockHeader.hash)
        assertThat(newHeader.hash).startsWith("0".repeat(difficulty))
        assertThat(newHeader.nonce).isGreaterThan(0)
        assertThat(newHeader.hash).isNotEqualTo(lastBlockHeader.hash)
    }

    @Test
    fun `should find same hash for same block header`() = runTest {

        // given
        val difficulty = 4
        every { minerProperties.difficulty } returns difficulty

        val fixedDateTime = LocalDateTime.of(2022, 1, 1, 0, 0);
        val fixedSource = InstantSource.fixed(fixedDateTime.toInstant(ZoneOffset.UTC));
        every { instantSource.instant() } returns fixedSource.instant()

        val lastBlockHeader = BlockHeader(
            index = 0,
            timestamp = 123456789L,
            previousHash = "0",
            hash = "abcd",
            nonce = 1
        )
        val lastBlock = Block(header = lastBlockHeader, transactions = emptyList())

        every { blockchain.getLastBlock() } returns lastBlock

        blockFactory = BlockFactory(blockchain, minerProperties, instantSource)

        // when
        val newHeader1 = blockFactory.createBlockHeader()
        val newHeader2 = blockFactory.createBlockHeader()

        // then
        assertThat(newHeader1.index).isEqualTo(1)
        assertThat(newHeader1.previousHash).isEqualTo(lastBlockHeader.hash)
        assertThat(newHeader1.hash).startsWith("0".repeat(difficulty))
        assertThat(newHeader1.nonce).isGreaterThan(0)
        assertThat(newHeader1.hash).isNotEqualTo(lastBlockHeader.hash)

        assertThat(newHeader2.index).isEqualTo(1)
        assertThat(newHeader2.previousHash).isEqualTo(lastBlockHeader.hash)
        assertThat(newHeader2.hash).startsWith("0".repeat(difficulty))
        assertThat(newHeader2.nonce).isGreaterThan(0)
        assertThat(newHeader2.hash).isNotEqualTo(lastBlockHeader.hash)

        assertThat(newHeader1.nonce).isEqualTo(newHeader2.nonce)
        assertThat(newHeader1.hash).isEqualTo(newHeader2.hash)
    }

}