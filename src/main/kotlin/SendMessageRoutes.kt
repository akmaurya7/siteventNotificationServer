import com.example.sitevent.Notification.FirebaseMessaging.SendMessageDto
import com.example.sitevent.Notification.FirebaseMessaging.toMessage
import com.google.firebase.messaging.FirebaseMessaging
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.sendNotification() {
    post("/send") {
        try {
            val body = call.receiveNullable<SendMessageDto>() ?: run {
                println("Received null body in /send")
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            println("Received send request: $body")
            FirebaseMessaging.getInstance().send(body.toMessage())
            call.respond(HttpStatusCode.OK)
        } catch (e: Exception) {
            println("Error in /send: ${e.message}")
            call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
        }
    }

    post("/broadcast") {
        try {
            val body = call.receiveNullable<SendMessageDto>() ?: run {
                println("Received null body in /broadcast")
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            println("Received broadcast request: $body")
            FirebaseMessaging.getInstance().send(body.toMessage())
            call.respond(HttpStatusCode.OK)
        } catch (e: Exception) {
            println("Error in /broadcast: ${e.message}")
            call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
        }
    }
}