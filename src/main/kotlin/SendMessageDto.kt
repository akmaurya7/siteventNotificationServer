package com.example.sitevent.Notification.FirebaseMessaging

import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import kotlinx.serialization.Serializable

@Serializable
data class SendMessageDto(
    val to: String?,
    val notification: NotificationBody,
    val data: Map<String, String> = emptyMap()  // Add data payload for custom fields
)

@Serializable
data class NotificationBody(
    val title: String,
    val body: String,
    val image: String? = null  // Optional image URL
)

fun SendMessageDto.toMessage(): Message {
    val builder = Message.builder()

    // Build notification
    val notificationBuilder = Notification.builder()
        .setTitle(notification.title)
        .setBody(notification.body)

    // Add image if available
    notification.image?.let {
        notificationBuilder.setImage(it)
    }

    // Add data payload
    if (data.isNotEmpty()) {
        builder.putAllData(data)
    }

    // Set recipient
    if (to == null) {
        builder.setTopic("chat")  // Broadcast to topic
    } else {
        builder.setToken(to)  // Send to specific device
    }

    return builder
        .setNotification(notificationBuilder.build())
        .build()
}

// Optional: Extension function for creating deep link messages
fun createDeepLinkMessage(
    to: String?,
    title: String,
    body: String,
    deepLinkRoute: String,
    imageUrl: String? = null,
    customData: Map<String, String> = emptyMap()
): SendMessageDto {
    val allData = customData.toMutableMap().apply {
        put("deepLink", deepLinkRoute)
    }

    return SendMessageDto(
        to = to,
        notification = NotificationBody(
            title = title,
            body = body,
            image = imageUrl
        ),
        data = allData
    )
}