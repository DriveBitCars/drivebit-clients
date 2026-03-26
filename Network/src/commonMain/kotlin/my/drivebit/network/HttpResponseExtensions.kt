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

suspend inline fun <reified T> HttpResponse.parseResponse(json: Json = defaultJson): T {
    val bodyString = bodyAsText()

    if (!status.isSuccess()) {
        val errorMessage =
            runCatching {
                val trimmedBody = bodyString.trim()
                if (trimmedBody.startsWith("{") && trimmedBody.endsWith("}")) {
                    val errorResponse = json.decodeFromString<ValidationErrorResponse>(trimmedBody)
                    val errorMessages = errorResponse.errors?.collectErrorMessages().orEmpty()
                    when {
                        !errorResponse.message.isNullOrBlank() -> errorResponse.message
                        errorMessages.isNotEmpty() -> errorMessages.joinToString(". ")
                        errorResponse.detail != null -> errorResponse.detail
                        errorResponse.error != null -> errorResponse.error
                        errorResponse.title != null -> errorResponse.title
                        else -> trimmedBody.trim('"').trim()
                    }
                } else {
                    trimmedBody.trim('"').trim()
                }
            }.getOrElse {
                bodyString.trim('"').trim()
            }
        throw NetworkException(status, errorMessage)
    }

    return json.decodeFromString<T>(bodyString)
}

suspend fun HttpResponse.consumeResponse() {
    val bodyString = bodyAsText()
    if (!status.isSuccess()) {
        val errorMessage =
            runCatching {
                val trimmedBody = bodyString.trim()
                if (trimmedBody.startsWith("{") && trimmedBody.endsWith("}")) {
                    val errorResponse = defaultJson.decodeFromString<ValidationErrorResponse>(trimmedBody)
                    val errorMessages = errorResponse.errors?.collectErrorMessages().orEmpty()
                    when {
                        !errorResponse.message.isNullOrBlank() -> errorResponse.message
                        errorMessages.isNotEmpty() -> errorMessages.joinToString(". ")
                        errorResponse.detail != null -> errorResponse.detail
                        errorResponse.error != null -> errorResponse.error
                        errorResponse.title != null -> errorResponse.title
                        else -> trimmedBody.trim('"').trim()
                    }
                } else {
                    trimmedBody.trim('"').trim()
                }
            }.getOrElse {
                bodyString.trim('"').trim()
            }
        throw NetworkException(status, errorMessage)
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
