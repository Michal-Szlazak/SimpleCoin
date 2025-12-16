package org.szlazakm.node.peer.message

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.szlazakm.node.domain.BaseMessage
import org.szlazakm.node.domain.Block
import org.szlazakm.node.domain.MessageType
import org.szlazakm.node.domain.NewBlockMessage

@Service
class UnorderedPeerMessenger(
    private val peerMessenger: PeerMessenger
) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val logger = LoggerFactory.getLogger(javaClass)

    private val channel = Channel<BaseMessage>(capacity = 100)

    init {
        scope.launch {
            consume()
        }
    }

    suspend fun sendUnordered(block: Block) {
        channel.send(NewBlockMessage(MessageType.NEW_BLOCK, block))
    }

    private suspend fun consume() {
        val buffer = mutableListOf<BaseMessage>()

        while (true) {
            val msg = channel.receive()
            buffer.add(msg)

            if (buffer.size >= 5) {
                flush(buffer)
            }
        }
    }

    private suspend fun flush(buffer: MutableList<BaseMessage>) {
        logger.info("Sending ${buffer.size} unordered messages")

        buffer.shuffle()

        for (msg in buffer) {
            peerMessenger.broadcast(msg)
            delay(1_000)
        }

        buffer.clear()
    }
}