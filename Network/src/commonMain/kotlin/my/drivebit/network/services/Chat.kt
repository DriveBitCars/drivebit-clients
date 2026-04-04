package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import my.drivebit.network.DEFAULT_BASE_URL
import my.drivebit.network.parseResponse

const val MESSAGE_ACTION_PAY_BOOKING = 1

const val SYSTEM_MESSAGE_PAYMENT_REQUEST = "PaymentRequest"

object SystemMessageTypeAsStringSerializer : KSerializer<String?> {
    private val backing = String.serializer().nullable

    override val descriptor: SerialDescriptor = backing.descriptor

    override fun deserialize(decoder: Decoder): String? {
        if (decoder is JsonDecoder) {
            val element = decoder.decodeJsonElement()
            if (element is JsonNull) return null
            val prim = element as? JsonPrimitive ?: return null
            return prim.contentOrNull?.takeIf { it.isNotBlank() }
        }
        return backing.deserialize(decoder)
    }

    override fun serialize(
        encoder: Encoder,
        value: String?,
    ) {
        backing.serialize(encoder, value)
    }
}

@Serializable
data class MessageActionBlockDto(
    val actionType: Int,
    val actionParameters: Map<String, String>? = null,
)

interface Chat {
    suspend fun hasUnread(): HasUnreadResponse

    suspend fun getChats(
        limit: Int = 20,
        offset: Int = 0,
        search: String? = null,
    ): ChatListResponse

    suspend fun getChat(chatId: String): ChatDetailDto

    suspend fun getMessages(
        chatId: String,
        limit: Int = 20,
        before: String? = null,
        after: String? = null,
    ): MessageListResponse

    suspend fun sendMessage(request: SendMessageRequest): MessageDto
}

@Serializable
data class HasUnreadResponse(
    val hasUnread: Boolean,
)

@Serializable
data class ChatListResponse(
    val chats: List<ChatListDto>? = null,
    val total: Int = 0,
    val hasMore: Boolean = false,
)

@Serializable
data class ChatListDto(
    val id: String,
    val participant: ChatParticipantDto,
    val lastMessage: ChatLastMessageDto? = null,
    val unreadCount: Int = 0,
    val updatedAt: String? = null,
)

@Serializable
data class ChatDetailDto(
    val id: String,
    val participant: ChatParticipantDto,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class ChatParticipantDto(
    val id: String,
    val name: String? = null,
    val avatar: String? = null,
)

@Serializable
data class ChatLastMessageDto(
    val id: String,
    val text: String? = null,
    val createdAt: String,
    val senderId: String,
    val sender: MessageSenderDto? = null,
)

@Serializable
data class MessageSenderDto(
    val id: String,
    val name: String? = null,
)

@Serializable
data class MessageListResponse(
    val messages: List<MessageDto>? = null,
    val hasMore: Boolean = false,
    val nextCursor: String? = null,
    val prevCursor: String? = null,
)

@Serializable
data class MessageDto(
    val id: String,
    val chatId: String,
    val sender: MessageSenderDto? = null,
    val text: String? = null,
    val isRead: Boolean = false,
    val createdAt: String,
    val isSystemMessage: Boolean = false,
    @JsonNames("messageActionBlock", "MessageActionBlock")
    val messageActionBlock: MessageActionBlockDto? = null,
    @JsonNames("bookingId", "BookingId") val bookingId: String? = null,
    @Serializable(with = SystemMessageTypeAsStringSerializer::class)
    @JsonNames("systemMessageType", "SystemMessageType")
    val systemMessageType: String? = null,
)

@Serializable
data class SendMessageRequest(
    val chatId: String,
    val text: String? = null,
)

fun MessageDto.payBookingIdForAction(): String? {
    if (!isSystemMessage) return null
    val booking =
        messageActionBlock?.actionParameters?.get("bookingId")?.takeIf { it.isNotBlank() }
            ?: bookingId?.takeIf { it.isNotBlank() }
            ?: return null
    if (messageActionBlock?.actionType == MESSAGE_ACTION_PAY_BOOKING) return booking
    if (systemMessageType?.equals(SYSTEM_MESSAGE_PAYMENT_REQUEST, ignoreCase = true) == true) {
        return booking
    }
    return null
}

class ChatImpl(
    private val httpClient: HttpClient,
) : Chat {
    override suspend fun hasUnread(): HasUnreadResponse {
        val url = "${DEFAULT_BASE_URL}Chat/has-unread"
        val response = httpClient.get(url)
        return response.parseResponse()
    }

    override suspend fun getChats(
        limit: Int,
        offset: Int,
        search: String?,
    ): ChatListResponse {
        val response =
            httpClient.get("${DEFAULT_BASE_URL}Chat") {
                parameter("limit", limit)
                parameter("offset", offset)
                search?.takeIf { it.isNotBlank() }?.let { parameter("search", it) }
            }
        return response.parseResponse()
    }

    override suspend fun getChat(chatId: String): ChatDetailDto {
        val url = "${DEFAULT_BASE_URL}Chat/$chatId"
        val response = httpClient.get(url)
        return response.parseResponse()
    }

    override suspend fun getMessages(
        chatId: String,
        limit: Int,
        before: String?,
        after: String?,
    ): MessageListResponse {
        val response =
            httpClient.get("${DEFAULT_BASE_URL}Chat/$chatId/messages") {
                parameter("limit", limit)
                before?.takeIf { it.isNotBlank() }?.let { parameter("before", it) }
                after?.takeIf { it.isNotBlank() }?.let { parameter("after", it) }
            }
        return response.parseResponse()
    }

    override suspend fun sendMessage(request: SendMessageRequest): MessageDto {
        val url = "${DEFAULT_BASE_URL}Chat/messages"
        val response =
            httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        return response.parseResponse()
    }
}
