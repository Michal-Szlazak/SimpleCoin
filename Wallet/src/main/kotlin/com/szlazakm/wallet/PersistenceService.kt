package com.szlazakm.wallet

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.szlazakm.wallet.data.Identity
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.IOException
import java.nio.file.*

@Service
class PersistenceService {

    private val mapper = jacksonObjectMapper()
    private val baseDir: Path = Paths.get("wallets").toAbsolutePath().normalize()
    private val logger = LoggerFactory.getLogger(PersistenceService::class.java)

    companion object {

        private const val FILE_EXTENSION = ".json"
    }

    init {
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir)
        }
    }

    fun persist(name: String, identity: Identity) {
        val file = baseDir.resolve("$name$FILE_EXTENSION")

        if (!file.normalize().startsWith(baseDir)) {
            throw SecurityException("Invalid filename path: $name")
        }

        if (Files.exists(file)) {
            throw FileAlreadyExistsException("Identity file $file already exists.")
        }

        try {
            val json = mapper.writeValueAsString(identity)
            Files.writeString(file, json, StandardOpenOption.CREATE_NEW)
        } catch (ex: IOException) {
            throw RuntimeException("Failed to persist identity: ${ex.message}", ex)
        }
    }

    fun retrieve(name: String): Identity {
        val file = baseDir.resolve("$name$FILE_EXTENSION")

        if (Files.notExists(file)) {
            throw NoSuchFileException("Identity file not found: $file")
        }

        val json = Files.readString(file)
        return mapper.readValue(json)
    }

    fun retrieveAll(): List<Identity> {
        if (Files.notExists(baseDir)) return emptyList()

        return Files.list(baseDir).use { stream ->
            stream
                .filter { Files.isRegularFile(it) && it.toString().endsWith(FILE_EXTENSION) }
                .map { path ->
                    try {
                        val json = Files.readString(path)
                        mapper.readValue<Identity>(json)
                    } catch (ex: Exception) {
                        logger.warn("Skipping invalid identity file: ${path.fileName} (${ex.message})")
                        null
                    }
                }
                .toList()
                .filterNotNull()
        }
    }
}