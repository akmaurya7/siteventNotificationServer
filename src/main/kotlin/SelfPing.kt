import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.server.application.*
import kotlinx.coroutines.*

// Call this from your Application.module() after you bind routes (and ensure /keepalive exists)
fun Application.startSelfPing(intervalMs: Long =  10 * 1000L) {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    val url = "http://127.0.0.1:$port/keepalive"
    val client = HttpClient(CIO)

    // Use a dedicated scope so we can cancel it on shutdown
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Launch the repeating job
    val job: Job = scope.launch {
        while (isActive) {
            try {
                client.get(url) // you can capture response if you want to inspect status/body
                environment.log.info("self-ping -> $url OK")
            } catch (t: Throwable) {
                environment.log.warn("self-ping failed: ${t.message}")
            }
            delay(intervalMs)
        }
    }

    // Cleanup on shutdown
    environment.monitor.subscribe(ApplicationStopping) {
        environment.log.info("Stopping self-ping job...")
        job.cancel()
        scope.cancel()
        client.close()
    }

    environment.log.info("Self-ping started -> $url every ${intervalMs}ms")
}
