// file: AutoGetScheduler.kt
package com.example

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import kotlinx.coroutines.*
import java.time.Instant

/**
 * Starts a background job that sends a GET to [url] every [intervalMs].
 * Defaults to the provided flashpay URL and 5 minutes interval.
 * Override the URL with env var AUTO_PING_URL if desired.
 */
fun Application.startAutoGetScheduler(
    url: String = System.getenv("AUTO_PING_URL") ?: "https://flashpay-frontend-amws.onrender.com/",
    intervalMs: Long = System.getenv("AUTO_PING_INTERVAL_MS")?.toLongOrNull() ?: 5 * 60 * 1000L
) {
    val client = HttpClient(CIO) {
        engine {
            // optional tuning
            requestTimeout = 60_000
        }
    }

    // Holder for the background Job so we can cancel on shutdown
    var backgroundJob: Job? = null

    // Start job when application starts
    environment.monitor.subscribe(ApplicationStarted) {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        backgroundJob = scope.launch {
            // run immediately, then wait interval; change ordering if you prefer wait-first
            while (isActive) {
                try {
                    val started = Instant.now()
                    val response: HttpResponse = client.get(url)
                    val body = response.bodyAsText() // careful: large bodies
                    environment.log.info("Auto-GET success: ${response.status.value} -> url=$url at $started (len=${body.length})")
                } catch (t: Throwable) {
                    environment.log.error("Auto-GET failed for url=$url: ${t.message}", t)
                }

                // cancellable wait
                delay(intervalMs)
            }
        }

        environment.log.info("Auto-GET scheduler started -> url=$url intervalMs=$intervalMs")
    }

    // Cancel job and close client when app is stopping
    environment.monitor.subscribe(ApplicationStopping) {
        environment.log.info("Stopping Auto-GET scheduler...")
        try {
            backgroundJob?.cancel()
        } catch (t: Throwable) {
            environment.log.warn("Error cancelling Auto-GET job: ${t.message}")
        }
        try {
            client.close()
        } catch (t: Throwable) {
            environment.log.warn("Error closing Auto-GET client: ${t.message}")
        }
    }
}
