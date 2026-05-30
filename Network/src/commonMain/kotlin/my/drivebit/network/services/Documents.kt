package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import my.drivebit.network.DEFAULT_BASE_URL
import my.drivebit.network.consumeResponse
import my.drivebit.network.parseResponse
import my.drivebit.utils.resolveMinioImageUrlForBrowser

interface Documents {
    suspend fun getDocuments(): List<Document>

    suspend fun uploadDocument(
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
        documentType: String,
        carId: String? = null,
    ): Document

    suspend fun getDocumentUrl(documentId: Int): String

    suspend fun deleteDocument(documentId: Int)
}

@Serializable
data class Document(
    val id: Int,
    val fileName: String? = null,
    @SerialName("carId")
    @JsonNames("carId", "CarId")
    val carId: String? = null,
    val type: String? = null,
    val status: String? = null,
    val validationResults: String? = null,
    val uploadDate: String? = null,
    val validatedAt: String? = null,
    val url: String? = null,
    val urlExpires: String? = null,
    val confidenceScore: Int = 0,
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
        documentType: String,
        carId: String?,
    ): Document {
        val uploadPath = DocumentUploadType.uploadPath(documentType)
        val url = "${DEFAULT_BASE_URL}Documents/$uploadPath"
        val response =
            httpClient.post(url) {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            if (DocumentUploadType.requiresCarId(documentType)) {
                                val resolvedCarId =
                                    carId?.takeIf { it.isNotBlank() }
                                        ?: error("CarId is required for STS documents")
                                append("CarId", resolvedCarId)
                            }
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

    override suspend fun getDocumentUrl(documentId: Int): String {
        val url = "${DEFAULT_BASE_URL}Documents/$documentId/temporary-link"
        val response = httpClient.get(url)
        val urlResponse: DocumentUrlResponse = response.parseResponse()
        return resolveMinioImageUrlForBrowser(urlResponse.url) ?: urlResponse.url
    }

    override suspend fun deleteDocument(documentId: Int) {
        val url = "${DEFAULT_BASE_URL}Documents/$documentId"
        val response = httpClient.delete(url)
        response.consumeResponse()
    }
}

@Serializable
data class DocumentUrlResponse(
    val url: String,
)
