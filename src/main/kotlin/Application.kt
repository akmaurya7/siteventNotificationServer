package com.example

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.ktor.server.application.*
import java.io.InputStream

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureRouting()

    // Load service account from resources
    val serviceAccount: InputStream = this::class.java.classLoader
        .getResourceAsStream("serviceAccountKey.json")
        ?: throw IllegalStateException("Firebase service account file not found")

    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .build()

    FirebaseApp.initializeApp(options)
}
