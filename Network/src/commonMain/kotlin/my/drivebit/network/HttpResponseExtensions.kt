package my.drivebit.network

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

val defaultJson: Json =
    Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
    }

@kotlinx.serialization.Serializable
data class ValidationErrorResponse(
    val errors: JsonObject? = null,
    val message: String? = null,
    val title: String? = null,
    val detail: String? = null,
    val error: String? = null,
)

@kotlinx.serialization.Serializable
data class ApiMessageDto(
    val success: Boolean? = null,
    val message: String? = null,
)

fun extractHttpErrorMessage(
    bodyString: String,
    json: Json = defaultJson,
): String {
    val trimmedBody = bodyString.trim()
    return runCatching {
        if (trimmedBody.startsWith("{") && trimmedBody.endsWith("}")) {
            val errorResponse = json.decodeFromString<ValidationErrorResponse>(trimmedBody)
            val errorMessages = errorResponse.errors?.collectErrorMessages().orEmpty()
            when {
                !errorResponse.message.isNullOrBlank() -> errorResponse.message
                errorMessages.isNotEmpty() -> errorMessages.joinToString(". ")
                !errorResponse.detail.isNullOrBlank() -> errorResponse.detail
                !errorResponse.error.isNullOrBlank() -> errorResponse.error
                !errorResponse.title.isNullOrBlank() -> errorResponse.title
                else -> trimmedBody.trim('"').trim()
            }
        } else {
            trimmedBody.trim('"').trim()
        }
    }.getOrElse {
        trimmedBody.trim('"').trim()
    }
}

suspend inline fun <reified T> HttpResponse.parseResponse(json: Json = defaultJson): T {
    val bodyString = bodyAsText()

    if (!status.isSuccess()) {
        throw NetworkException(status, extractHttpErrorMessage(bodyString, json))
    }

    return json.decodeFromString<T>(bodyString)
}

suspend fun HttpResponse.consumeResponse(json: Json = defaultJson) {
    val bodyString = bodyAsText()
    if (!status.isSuccess()) {
        throw NetworkException(status, extractHttpErrorMessage(bodyString, json))
    }
}

suspend fun HttpResponse.consumeMessageResponse(
    defaultError: String = "Операция не выполнена",
    json: Json = defaultJson,
) {
    val bodyString = bodyAsText()
    if (!status.isSuccess()) {
        throw NetworkException(status, extractHttpErrorMessage(bodyString, json))
    }

    val trimmedBody = bodyString.trim()
    if (!trimmedBody.startsWith("{") || !trimmedBody.endsWith("}")) {
        return
    }

    val messageDto =
        runCatching {
            json.decodeFromString<ApiMessageDto>(trimmedBody)
        }.getOrNull() ?: return

    if (messageDto.success == false) {
        throw NetworkException(
            status,
            messageDto.message?.takeIf { it.isNotBlank() } ?: defaultError,
        )
    }
}

class NetworkException(
    val statusCode: HttpStatusCode,
    override val message: String,
) : Exception(message) {
    val statusCodeValue: Int
        get() = statusCode.value
}

fun JsonObject.collectErrorMessages(): List<String> {
    val collected = mutableListOf<String>()
    values.forEach { element ->
        when (element) {
            is JsonPrimitive -> element.contentOrNull?.takeIf { it.isNotBlank() }?.let(collected::add)
            is JsonArray ->
                element.forEach { item ->
                    if (item is JsonPrimitive) {
                        item.contentOrNull?.takeIf { it.isNotBlank() }?.let(collected::add)
                    }
                }
            is JsonObject -> collected += element.collectErrorMessages()
            else -> {
            }
        }
    }
    return collected
}
