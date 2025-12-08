package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.Serializable
import my.drivebit.network.DEFAULT_BASE_URL
import my.drivebit.network.parseResponse

interface Photo {
    suspend fun getAvatar(): AvatarResponse

    suspend fun uploadAvatar(
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
    ): AvatarResponse
}

@Serializable
data class AvatarResponse(
    val url: String,
)

class PhotoImpl(
    private val httpClient: HttpClient,
) : Photo {
    override suspend fun getAvatar(): AvatarResponse {
        val url = "${DEFAULT_BASE_URL}Photo/avatar/my"
        val response = httpClient.get(url)
        return response.parseResponse()
    }

    override suspend fun uploadAvatar(
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
    ): AvatarResponse {
        val url = "${DEFAULT_BASE_URL}Photo/avatar/my"
        val response =
            httpClient.post(url) {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                "file",
                                fileBytes,
                                Headers.build {
                                    append(HttpHeaders.ContentType, contentType)
                                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                                },
                            )
                        },
                    ),
                )
            }
        return response.parseResponse()
    }
}
