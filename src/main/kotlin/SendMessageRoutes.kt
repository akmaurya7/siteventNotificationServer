import com.example.sitevent.Notification.FirebaseMessaging.SendMessageDto
import com.example.sitevent.Notification.FirebaseMessaging.toMessage
import com.google.firebase.messaging.FirebaseMessaging
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receiveNullable
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.sendNotification() {
    post("/send") {
        val body = call.receiveNullable<SendMessageDto>() ?: run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        FirebaseMessaging.getInstance().send(body.toMessage())
        call.respond(HttpStatusCode.OK)
    }

    post("/broadcast") {
        val body = call.receiveNullable<SendMessageDto>() ?: run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        FirebaseMessaging.getInstance().send(body.toMessage())
        call.respond(HttpStatusCode.OK)
    }
}