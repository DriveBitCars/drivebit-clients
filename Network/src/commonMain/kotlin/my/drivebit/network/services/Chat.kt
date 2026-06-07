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
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import my.drivebit.network.DEFAULT_BASE_URL
import my.drivebit.network.parseResponse

@Serializable
enum class ActionTypeEnum {
    None,
    PayBooking,
    LeaveReviewForRenter,
    LeaveReviewForCar,
    ConfirmBooking,
    DownloadContract,
    SignContract,
}

object ActionTypeEnumSerializer : KSerializer<ActionTypeEnum> {
    private val backing = ActionTypeEnum.serializer()

    override val descriptor: SerialDescriptor = backing.descriptor

    override fun deserialize(decoder: Decoder): ActionTypeEnum {
        if (decoder is JsonDecoder) {
            val element = decoder.decodeJsonElement()
            if (element is JsonNull) return ActionTypeEnum.None
            val prim = element as? JsonPrimitive ?: return ActionTypeEnum.None
            val raw = prim.contentOrNull?.trim().orEmpty()
            if (raw.isNotEmpty()) {
                ActionTypeEnum.entries.find { it.name == raw }?.let { return it }
            }
            prim.intOrNull?.let { ord ->
                return ActionTypeEnum.entries.getOrNull(ord) ?: ActionTypeEnum.None
            }
            return ActionTypeEnum.None
        }
        return backing.deserialize(decoder)
    }

    override fun serialize(
        encoder: Encoder,
        value: ActionTypeEnum,
    ) {
        backing.serialize(encoder, value)
    }
}

@Serializable
enum class SystemMessageType {
    BookingRequest,
    BookingConfirmed,
    BookingDeclined,
    BookingActive,
    BookingCompleted,
    BookingCancelledByRenter,
    BookingCancelledByOwner,
    BookingExpired,
    PaymentRequest,
    PaymentReceived,
    BookingPaid,
    BookingPaymentExpired,
    BookingRefunded,
    BookingStatusChanged,
    General,
}

object SystemMessageTypeNullableSerializer : KSerializer<SystemMessageType?> {
    private val backing = SystemMessageType.serializer().nullable

    override val descriptor: SerialDescriptor = backing.descriptor

    override fun deserialize(decoder: Decoder): SystemMessageType? {
        if (decoder is JsonDecoder) {
            val element = decoder.decodeJsonElement()
            if (element is JsonNull) return null
            val prim = element as? JsonPrimitive ?: return null
            val raw = prim.contentOrNull?.trim() ?: return null
            if (raw.isEmpty()) return null
            SystemMessageType.entries.find { it.name == raw }?.let { return it }
            prim.intOrNull?.let { ord ->
                return SystemMessageType.entries.getOrNull(ord)
            }
            return null
        }
        return backing.deserialize(decoder)
    }

    override fun serialize(
        encoder: Encoder,
        value: SystemMessageType?,
    ) {
        backing.serialize(encoder, value)
    }
}

@Serializable
data class MessageActionBlockDto(
    @Serializable(with = ActionTypeEnumSerializer::class)
    val actionType: ActionTypeEnum = ActionTypeEnum.None,
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
    @Serializable(with = SystemMessageTypeNullableSerializer::class)
    @JsonNames("systemMessageType", "SystemMessageType")
    val systemMessageType: SystemMessageType? = null,
)

@Serializable
data class SendMessageRequest(
    val chatId: String,
    val text: String? = null,
)

private val contractDownloadUrlRegex =
    Regex("""(?:https?://(?:www\.)?drivebit\.ru)?/download-booking-contract\?bookingId=([0-9a-fA-F-]{36})""")

fun extractBookingIdFromContractDownloadUrl(text: String): String? =
    contractDownloadUrlRegex
        .find(text)
        ?.groupValues
        ?.getOrNull(1)
        ?.takeIf { it.isNotBlank() }

fun contractDownloadPagePath(bookingId: String): String = "/download-booking-contract?bookingId=$bookingId"

fun contractDownloadPageUrl(bookingId: String): String = "https://drivebit.ru${contractDownloadPagePath(bookingId)}"

fun leaveReviewPagePath(carId: String): String = "/leave-review?carId=$carId"

fun leaveReviewPageUrl(carId: String): String = "https://drivebit.ru${leaveReviewPagePath(carId)}"

fun MessageDto.payBookingIdForAction(): String? {
    if (!isSystemMessage) return null
    val booking =
        messageActionBlock?.actionParameters?.get("bookingId")?.takeIf { it.isNotBlank() }
            ?: bookingId?.takeIf { it.isNotBlank() }
            ?: return null
    when (messageActionBlock?.actionType) {
        ActionTypeEnum.PayBooking,
        ActionTypeEnum.ConfirmBooking,
        -> return booking
        else -> {}
    }
    if (systemMessageType == SystemMessageType.PaymentRequest) {
        return booking
    }
    return null
}

fun MessageDto.contractBookingIdForAction(): String? {
    if (!isSystemMessage) return null
    val bookingFromMessage =
        messageActionBlock?.actionParameters?.get("bookingId")?.takeIf { it.isNotBlank() }
            ?: bookingId?.takeIf { it.isNotBlank() }
    when (messageActionBlock?.actionType) {
        ActionTypeEnum.DownloadContract,
        ActionTypeEnum.SignContract,
        -> return bookingFromMessage
        else -> {}
    }
    return text?.let(::extractBookingIdFromContractDownloadUrl)
}

private fun MessageDto.bookingIdFromActionOrMessage(): String? =
    messageActionBlock?.actionParameters?.get("bookingId")?.takeIf { it.isNotBlank() }
        ?: bookingId?.takeIf { it.isNotBlank() }

fun MessageDto.leaveReviewCarIdForAction(bookingById: Map<String, BookingDTO> = emptyMap()): String? {
    if (!isSystemMessage) return null

    messageActionBlock
        ?.actionParameters
        ?.get("carId")
        ?.takeIf { it.isNotBlank() }
        ?.let { return it }

    val isCarReviewAction =
        when (messageActionBlock?.actionType) {
            ActionTypeEnum.LeaveReviewForCar -> true
            ActionTypeEnum.LeaveReviewForRenter -> false
            else ->
                systemMessageType == SystemMessageType.BookingCompleted &&
                    text.orEmpty().contains("об аренде", ignoreCase = true)
        }
    if (!isCarReviewAction) return null

    val bookingKey = bookingIdFromActionOrMessage() ?: return null
    return bookingById[bookingKey]?.carId?.takeIf { it.isNotBlank() }
}

fun MessageDto.shouldShowLeaveReviewForRenter(): Boolean {
    if (!isSystemMessage) return false
    when (messageActionBlock?.actionType) {
        ActionTypeEnum.LeaveReviewForRenter -> return true
        ActionTypeEnum.LeaveReviewForCar -> return false
        else -> {}
    }
    return systemMessageType == SystemMessageType.BookingCompleted &&
        text.orEmpty().contains("об арендаторе", ignoreCase = true)
}

fun MessageDto.isLeaveReviewForCarAction(): Boolean {
    if (!isSystemMessage) return false
    when (messageActionBlock?.actionType) {
        ActionTypeEnum.LeaveReviewForCar -> return true
        ActionTypeEnum.LeaveReviewForRenter -> return false
        else -> {}
    }
    return systemMessageType == SystemMessageType.BookingCompleted &&
        text.orEmpty().contains("об аренде", ignoreCase = true)
}

fun MessageDto.leaveReviewBookingIdForAction(): String? {
    if (!isSystemMessage) return null
    when (messageActionBlock?.actionType) {
        ActionTypeEnum.LeaveReviewForCar,
        ActionTypeEnum.LeaveReviewForRenter,
        -> return bookingIdFromActionOrMessage()
        else -> {}
    }
    if (systemMessageType == SystemMessageType.BookingCompleted &&
        (
            text.orEmpty().contains("об аренде", ignoreCase = true) ||
                text.orEmpty().contains("об арендаторе", ignoreCase = true)
        )
    ) {
        return bookingIdFromActionOrMessage()
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
