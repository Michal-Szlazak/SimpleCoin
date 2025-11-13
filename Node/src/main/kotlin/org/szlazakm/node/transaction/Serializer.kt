package org.szlazakm.node.transaction

import org.szlazakm.node.domain.Transaction
import java.nio.ByteBuffer
import kotlin.collections.forEach
import kotlin.collections.forEachIndexed
import kotlin.text.toByteArray

object Serializer {

    fun serializeTransactionForSigning(tx: Transaction, inputIndex: Int, prevOutputAddress: String): ByteArray {
        val buffer = ByteBuffer.allocate(16_384)

        buffer.putInt(1)

        buffer.putInt(tx.inputs.size)
        tx.inputs.forEachIndexed { i, inp ->
            buffer.put(inp.txId.toByteArray())
            buffer.putInt(inp.outputIndex)
            val script = if (i == inputIndex) prevOutputAddress.toByteArray() else ByteArray(0)
            buffer.putInt(script.size)
            buffer.put(script)
        }

        buffer.putInt(tx.outputs.size)
        tx.outputs.forEach { out ->
            buffer.putDouble(out.value)
            val addrBytes = out.address.toByteArray()
            buffer.putInt(addrBytes.size)
            buffer.put(addrBytes)
        }

        buffer.put(tx.publicKey.toByteArray())

        val bytes = ByteArray(buffer.position())
        System.arraycopy(buffer.array(), 0, bytes, 0, bytes.size)
        return bytes
    }
}