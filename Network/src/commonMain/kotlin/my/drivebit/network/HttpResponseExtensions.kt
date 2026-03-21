package my.drivebit.network

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

val defaultJson: Json =
    Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
    }

@kotlinx.serialization.Serializable
data class ValidationErrorResponse(
    val errors: Map<String, List<String>>? = null,
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
                    val errorMessages = errorResponse.errors?.flatMap { (_, messages) -> messages } ?: emptyList()
                    when {
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
                    val errorMessages = errorResponse.errors?.flatMap { (_, messages) -> messages } ?: emptyList()
                    when {
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
