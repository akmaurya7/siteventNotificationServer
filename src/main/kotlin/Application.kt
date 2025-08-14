package com.example

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.ktor.server.application.*
import java.io.ByteArrayInputStream

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureRouting()



    val serviceAccountJson = """
        {
          "type": "${System.getenv("type")}",
          "project_id": "${System.getenv("project_id")}",
          "private_key_id": "${System.getenv("private_key_id")}",
          "private_key": "${System.getenv("private_key")?.replace("\\n", "\n")}",
          "client_email": "${System.getenv("client_email")}",
          "client_id": "${System.getenv("client_id")}",
          "auth_uri": "${System.getenv("auth_uri")}",
          "token_uri": "${System.getenv("token_uri")}",
          "auth_provider_x509_cert_url": "${System.getenv("auth_provider_x509_cert_url")}",
          "client_x509_cert_url": "${System.getenv("client_x509_cert_url")}",
          "universe_domain": "${System.getenv("universe_domain")}"
        }
    """.trimIndent()


    val serviceAccountStream = ByteArrayInputStream(serviceAccountJson.toByteArray())
    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
        .build()

    FirebaseApp.initializeApp(options)
}
