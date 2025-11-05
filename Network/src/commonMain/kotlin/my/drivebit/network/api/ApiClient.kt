package my.drivebit.network.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val message: String,
    val code: Int? = null,
)

class NetworkException(
    val error: ApiError,
    val statusCode: HttpStatusCode,
) : Exception(error.message)

class ApiClient(
    val httpClient: HttpClient,
    val baseUrl: String,
) {
    suspend inline fun <reified T> get(path: String): T =
        handleResponse {
            httpClient.get("$baseUrl$path")
        }.body()

    suspend inline fun <reified T> post(
        path: String,
        body: Any? = null,
    ): T =
        handleResponse {
            httpClient.post("$baseUrl$path") {
                if (body != null) {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }
            }
        }.body()

    suspend inline fun <reified T> put(
        path: String,
        body: Any? = null,
    ): T =
        handleResponse {
            httpClient.put("$baseUrl$path") {
                if (body != null) {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }
            }
        }.body()

    suspend fun delete(path: String) {
        handleResponse {
            httpClient.delete("$baseUrl$path")
        }
    }

    suspend fun handleResponse(block: suspend () -> HttpResponse): HttpResponse {
        val response = block()
        val statusCode = response.status.value
        if (statusCode !in 200..299) {
            val error =
                try {
                    response.body<ApiError>()
                } catch (e: Exception) {
                    ApiError(
                        message = response.status.description,
                        code = statusCode,
                    )
                }
            throw NetworkException(error, response.status)
        }
        return response
    }
}
