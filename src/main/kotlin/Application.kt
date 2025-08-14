package com.example

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.gson.Gson
import io.ktor.server.application.*
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureRouting()

    // List of keys we expect from Render environment variables
    val keys = listOf(
        "type", "project_id", "private_key_id", "private_key",
        "client_email", "client_id", "auth_uri", "token_uri",
        "auth_provider_x509_cert_url", "client_x509_cert_url", "universe_domain"
    )

    // Collect env vars and detect any missing ones early
    val missing = keys.filter { System.getenv(it).isNullOrBlank() }
    require(missing.isEmpty()) { "Missing required env vars: $missing" }

    // Read and normalize private_key: convert literal "\n" sequences to real newlines
    val rawPrivateKey = System.getenv("private_key") ?: ""
    val normalizedPrivateKey = rawPrivateKey.replace("\\n", "\n")

    // Build a map for the service account JSON
    val map = mapOf(
        "type" to System.getenv("type"),
        "project_id" to System.getenv("project_id"),
        "private_key_id" to System.getenv("private_key_id"),
        "private_key" to normalizedPrivateKey,
        "client_email" to System.getenv("client_email"),
        "client_id" to System.getenv("client_id"),
        "auth_uri" to System.getenv("auth_uri"),
        "token_uri" to System.getenv("token_uri"),
        "auth_provider_x509_cert_url" to System.getenv("auth_provider_x509_cert_url"),
        "client_x509_cert_url" to System.getenv("client_x509_cert_url"),
        "universe_domain" to System.getenv("universe_domain")
    )

    // Convert map to valid JSON using Gson (it will escape newlines etc. correctly)
    val serviceAccountJson = Gson().toJson(map)

    val serviceAccountStream = ByteArrayInputStream(serviceAccountJson.toByteArray(StandardCharsets.UTF_8))
    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
        .build()

    FirebaseApp.initializeApp(options)
}
