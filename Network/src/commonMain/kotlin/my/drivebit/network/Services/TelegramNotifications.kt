package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import kotlinx.serialization.Serializable
import my.drivebit.network.DEFAULT_BASE_URL
import my.drivebit.network.consumeResponse
import my.drivebit.network.parseResponse

interface TelegramNotifications {
    suspend fun getLinkStatus(): TelegramBindingStatus

    suspend fun createLink(): TelegramLinkStart

    suspend fun unlink()
}

@Serializable
data class TelegramBindingStatus(
    val isLinked: Boolean,
    val telegramUsername: String? = null,
    val linkedAt: String? = null,
)

@Serializable
data class TelegramLinkStart(
    val token: String,
    val code: String,
    val deepLinkUrl: String,
    val expiresAt: String,
)

class TelegramNotificationsImpl(
    private val httpClient: HttpClient,
) : TelegramNotifications {
    override suspend fun getLinkStatus(): TelegramBindingStatus {
        val response = httpClient.get("${DEFAULT_BASE_URL}Notifications/Telegram/link/status")
        return response.parseResponse()
    }

    override suspend fun createLink(): TelegramLinkStart {
        val response = httpClient.post("${DEFAULT_BASE_URL}Notifications/Telegram/link")
        return response.parseResponse()
    }

    override suspend fun unlink() {
        val response = httpClient.post("${DEFAULT_BASE_URL}Notifications/Telegram/unlink")
        response.consumeResponse()
    }
}
