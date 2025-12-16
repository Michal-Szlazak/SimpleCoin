//package org.szlazakm.node.transaction
//
//import io.mockk.every
//import io.mockk.mockk
//import org.assertj.core.api.Assertions.assertThat
//import org.bouncycastle.jce.provider.BouncyCastleProvider
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.params.ParameterizedTest
//import org.junit.jupiter.params.provider.Arguments
//import org.junit.jupiter.params.provider.MethodSource
//import org.szlazakm.node.config.MinerProperties
//import org.szlazakm.node.domain.Transaction
//import org.szlazakm.node.domain.TxInput
//import org.szlazakm.node.domain.TxOutput
//import org.szlazakm.node.utils.KeyService
//import java.security.Security
//import java.util.Base64
//import java.util.stream.Stream
//
//class TransactionValidatorTest {
//
//    private lateinit var utxoService: UTXOService
//    private lateinit var minerProperties: MinerProperties
//    private lateinit var transactionValidator: TransactionValidator
//
//    init {
//        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
//            Security.addProvider(BouncyCastleProvider())
//        }
//    }
//
//    @BeforeEach
//    fun setup() {
//        utxoService = mockk()
//        minerProperties = mockk()
//        transactionValidator = TransactionValidator(utxoService, minerProperties)
//
//        every { minerProperties.reward } returns REWARD
//    }
//
//    @ParameterizedTest(name = "{index} => {0}")
//    @MethodSource("transactionScenarios")
//    fun validateTransactionScenario(
//        label: String,
//        inputs: List<TxInput>,
//        outputs: List<TxOutput>,
//        previousOutputs: List<TxOutput>,
//        expectedSuccess: Boolean
//    ) {
//
//        val prevOutputAddress = KeyService.sha256Ripemd160(keyPair.public.encoded)
//
//        val transaction = Transaction(
//            id = "1",
//            inputs = inputs,
//            outputs = outputs,
//            publicKey = encoder.encodeToString(keyPair.public.encoded)
//        )
//
//        if(!transaction.isCoinbase()) {
//            inputs.forEachIndexed { i, txInput ->
//                val signature = KeyService.signTransactionInput(
//                    keyPair.private.encoded, transaction, i, prevOutputAddress
//                )
//                txInput.signature = encoder.encodeToString(signature)
//            }
//        }
//
//        previousOutputs.forEachIndexed { i, prevOutput ->
//            every { utxoService.getUtxo(inputs[i].txId) } returns prevOutput
//        }
//
//        val result = transactionValidator.validateTransaction(transaction)
//        assertThat(result.isSuccess).isEqualTo(expectedSuccess)
//        println(result)
//    }
//
//    @Test
//    fun validateTransactionsScenario(
//
//    ) {
//
//    }
//
//    companion object {
//
//        init {
//            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
//                Security.addProvider(BouncyCastleProvider())
//            }
//        }
//
//        val keyPair = KeyService.generateKeyPair()
//        private const val REWARD = 3.0
//        private val encoder = Base64.getEncoder()
//
//        @JvmStatic
//        fun transactionScenarios(): Stream<Arguments> {
//
//            val address = KeyService.sha256Ripemd160(keyPair.public.encoded)
//
//            val txInput1 = TxInput("1", 0, "")
//            val txInput2 = TxInput("1", 1, "")
//            val coinbaseInput = TxInput("0".repeat(64), -1, "coinbase_not_important")
//
//            val txOutput1 = TxOutput(3.0, address)
//            val txOutput2 = TxOutput(4.0, address)
//            val prevOutput = TxOutput(3.0, address)
//            val prevOutputInvalid = TxOutput(3.0, "invalid address")
//
//            val coinbaseOutput1 = TxOutput(REWARD, address)
//            val coinbaseOutput2 = TxOutput(REWARD + 1, address)
//
//            return Stream.of(
//                Arguments.of(
//                    "Valid transaction with 2 inputs",
//                    listOf(txInput1, txInput2),
//                    listOf(txOutput1, txOutput1),
//                    listOf(prevOutput, prevOutput),
//                    true
//                ),
//                Arguments.of(
//                    "Valid transaction with 1 input",
//                    listOf(txInput1),
//                    listOf(txOutput1),
//                    listOf(prevOutput),
//                    true
//                ),
//                Arguments.of(
//                    "Valid coinbase transaction",
//                    listOf(coinbaseInput),
//                    listOf(coinbaseOutput1),
//                    listOf<TxOutput>(),
//                    true
//                ),
//                Arguments.of(
//                    "Coinbase transaction with invalid output",
//                    listOf(coinbaseInput),
//                    listOf(coinbaseOutput2),
//                    listOf<TxOutput>(),
//                    false
//                ),
//                Arguments.of(
//                    "Invalid transaction with unspent inputs",
//                    listOf(txInput1),
//                    listOf<TxOutput>(),
//                    listOf(prevOutput),
//                    false
//                ),
//                Arguments.of(
//                    "Invalid transaction with outputs without inputs",
//                    listOf<TxInput>(),
//                    listOf(txOutput1),
//                    listOf<TxOutput>(),
//                    false
//                ),
//                Arguments.of(
//                    "Invalid transaction with overspent inputs",
//                    listOf(txInput1),
//                    listOf(txOutput2),
//                    listOf(prevOutput),
//                    false
//                ),
//                Arguments.of(
//                    "Invalid transaction with double spending",
//                    listOf(txInput1, txInput1),
//                    listOf(txOutput1, txOutput2),
//                    listOf(prevOutput, prevOutput),
//                    false
//                ),
//                Arguments.of(
//                    "Invalid transaction with wrong previous output address",
//                    listOf(txInput1, txInput2),
//                    listOf(txOutput1, txOutput1),
//                    listOf(prevOutputInvalid, prevOutput),
//                    false
//                )
//            )
//        }
//
//        @JvmStatic
//        fun transactionsScenarios(): Stream<Arguments> {
//
//            val address = KeyService.sha256Ripemd160(keyPair.public.encoded)
//
//            val txInput1 = TxInput("1", 0, "")
//            val txInput2 = TxInput("1", 1, "")
//            val txInput3 = TxInput("2", 0, "")
//            val txInput4 = TxInput("2", 1, "")
//            val coinbaseInput = TxInput("0".repeat(64), -1, "coinbase_not_important")
//
//            val txOutput1 = TxOutput(3.0, address)
//            val txOutput2 = TxOutput(4.0, address)
//            val prevOutput = TxOutput(3.0, address)
//
//            val coinbaseOutput1 = TxOutput(REWARD, address)
//            val coinbaseOutput2 = TxOutput(REWARD + 1, address)
//
//            val validTransaction1 = Transaction(
//                id = "1",
//                inputs = listOf(txInput1, txInput2),
//                outputs = listOf(txOutput1, txOutput2),
//                publicKey = encoder.encodeToString(keyPair.public.encoded)
//            )
//            val validTransaction2 = Transaction(
//                id = "1",
//                inputs = listOf(txInput3, txInput4),
//                outputs = listOf(txOutput1, txOutput2),
//                publicKey = encoder.encodeToString(keyPair.public.encoded)
//            )
//
//            return Stream.of(
//                Arguments.of()
//            )
//        }
//
//        fun generateSignatures(transaction: Transaction) {
//
//            val prevOutputAddress = KeyService.sha256Ripemd160(keyPair.public.encoded)
//
//            transaction.inputs.forEachIndexed { i, txInput ->
//                val signature = KeyService.signTransactionInput(
//                    keyPair.private.encoded, transaction, i, prevOutputAddress
//                )
//                txInput.signature = encoder.encodeToString(signature)
//            }
//        }
//    }
//
//}