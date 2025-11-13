package com.szlazakm.wallet.wallet

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.szlazakm.wallet.domain.Identity
import com.szlazakm.wallet.domain.NamedIdentity
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.IOException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

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

    fun persist(name: String, identity: Identity): Result<Unit> {
        val file = baseDir.resolve("$name$FILE_EXTENSION")

        if (!file.normalize().startsWith(baseDir)) {
            return Result.failure(IOException("$name not in $baseDir"))
        }

        if (Files.exists(file)) {
            return Result.failure(IOException("$name already exists"))
        }

        try {
            val json = mapper.writeValueAsString(identity)
            Files.writeString(file, json, StandardOpenOption.CREATE_NEW)
        } catch (ex: IOException) {
            return Result.failure(ex)
        }

        return Result.success(Unit)
    }

    fun retrieve(name: String): Identity {
        val file = baseDir.resolve("$name$FILE_EXTENSION")

        if (Files.notExists(file)) {
            throw NoSuchFileException("Identity file not found: $file")
        }

        val json = Files.readString(file)
        return mapper.readValue(json)
    }

    fun retrieveAll(): List<NamedIdentity> {
        if (Files.notExists(baseDir)) return emptyList()

        return Files.list(baseDir).use { stream ->
            stream
                .filter { Files.isRegularFile(it) && it.toString().endsWith(FILE_EXTENSION) }
                .map { path ->
                    try {
                        val json = Files.readString(path)
                        val identity = mapper.readValue<Identity>(json)
                        return@map NamedIdentity(identity, path.fileName.toString().replace(FILE_EXTENSION, ""))
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