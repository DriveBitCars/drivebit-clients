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

suspend inline fun <reified T> HttpResponse.parseResponse(json: Json = defaultJson): T {
    val bodyString = bodyAsText()

    if (!status.isSuccess()) {
        val errorMessage = bodyString.trim('"').trim()
        throw NetworkException(status, errorMessage)
    }

    return json.decodeFromString<T>(bodyString)
}

class NetworkException(
    val statusCode: HttpStatusCode,
    override val message: String,
) : Exception(message)
