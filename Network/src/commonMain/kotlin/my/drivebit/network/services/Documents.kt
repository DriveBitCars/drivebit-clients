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

interface Documents {
    suspend fun getDocuments(): List<Document>

    suspend fun uploadDocument(
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
    ): Document

    suspend fun getDocumentUrl(documentId: String): String
}

@Serializable
data class Document(
    val id: String,
    val name: String? = null,
    val type: String? = null,
    val url: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

class DocumentsImpl(
    private val httpClient: HttpClient,
) : Documents {
    override suspend fun getDocuments(): List<Document> {
        val url = "${DEFAULT_BASE_URL}Documents"
        val response = httpClient.get(url)
        return response.parseResponse()
    }

    override suspend fun uploadDocument(
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
    ): Document {
        val url = "${DEFAULT_BASE_URL}Documents/upload"
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

    override suspend fun getDocumentUrl(documentId: String): String {
        val url = "${DEFAULT_BASE_URL}Documents/$documentId/temporary-link"
        val response = httpClient.get(url)
        val urlResponse: DocumentUrlResponse = response.parseResponse()
        return urlResponse.url
    }
}

@Serializable
data class DocumentUrlResponse(
    val url: String,
)
