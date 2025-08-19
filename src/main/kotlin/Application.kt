package com.example

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.ktor.server.application.*
import startSelfPing
import java.io.ByteArrayInputStream
import java.io.File
import java.util.Base64

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureRouting()
    startSelfPing()

    val credentials = try {
        loadGoogleCredentials(this)
    } catch (e: IllegalStateException) {
        environment.log.error("Failed to load Google credentials: ${e.message}")
        throw e
    }

    val options = FirebaseOptions.builder()
        .setCredentials(credentials)
        .build()

    FirebaseApp.initializeApp(options)
    environment.log.info("Firebase initialized successfully.")
}

/**
 * Tries the following in order:
 *  1) classpath resource "service_account_key.json"
 *  2) file path from env GOOGLE_SERVICE_ACCOUNT_PATH
 *  3) raw JSON from env GOOGLE_SERVICE_ACCOUNT_JSON
 *  4) base64 JSON from env SERVICE_ACCOUNT_BASE64
 *
 * Throws IllegalStateException if none is available.
 */
fun loadGoogleCredentials(app: Application): GoogleCredentials {
    val log = app.environment.log

    // 1) classpath resource
    val classpathStream = object {}.javaClass.classLoader.getResourceAsStream("service_account_key.json")
    if (classpathStream != null) {
        log.info("Using service account from classpath resource: service_account_key.json")
        return GoogleCredentials.fromStream(classpathStream)
    }

    // 2) file path via env var GOOGLE_SERVICE_ACCOUNT_PATH
    System.getenv("GOOGLE_SERVICE_ACCOUNT_PATH")?.let { path ->
        val f = File(path)
        if (f.exists()) {
            log.info("Using service account from file path: $path")
            return GoogleCredentials.fromStream(f.inputStream())
        } else {
            log.warn("GOOGLE_SERVICE_ACCOUNT_PATH set but file not found: $path")
        }
    }

    // 3) raw JSON via env var GOOGLE_SERVICE_ACCOUNT_JSON
    System.getenv("GOOGLE_SERVICE_ACCOUNT_JSON")?.let { json ->
        if (json.isNotBlank()) {
            log.info("Using service account from env: GOOGLE_SERVICE_ACCOUNT_JSON")
            return GoogleCredentials.fromStream(json.byteInputStream())
        }
    }

    // 4) base64-encoded JSON via env var SERVICE_ACCOUNT_BASE64
    System.getenv("SERVICE_ACCOUNT_BASE64")?.let { b64 ->
        if (b64.isNotBlank()) {
            try {
                val decoded = Base64.getDecoder().decode(b64)
                log.info("Using service account from env: SERVICE_ACCOUNT_BASE64")
                return GoogleCredentials.fromStream(ByteArrayInputStream(decoded))
            } catch (ex: IllegalArgumentException) {
                log.warn("SERVICE_ACCOUNT_BASE64 is set but failed to decode base64: ${ex.message}")
            }
        }
    }

    throw IllegalStateException(
        "Firebase service account not found. Provide one of:\n" +
                " - src/main/resources/service_account_key.json (classpath)\n" +
                " - env GOOGLE_SERVICE_ACCOUNT_PATH (path to file inside container)\n" +
                " - env GOOGLE_SERVICE_ACCOUNT_JSON (raw JSON)\n" +
                " - env SERVICE_ACCOUNT_BASE64 (base64-encoded JSON)"
    )
}
