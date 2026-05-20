package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import my.drivebit.network.parseResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class BookingContractDownloadDtoTest {
    @Test
    fun `getContract parses camelCase response`() =
        runTest {
            val bookingId = "550e8400-e29b-41d4-a716-446655440000"
            val mockEngine =
                MockEngine {
                    respond(
                        content =
                            """
                            {
                              "contractNumber": 42,
                              "fileName": "contract.docx",
                              "downloadUrl": "https://minio.example/contract.docx",
                              "urlExpiresAt": "2026-05-20T12:00:00Z",
                              "generatedAt": "2026-05-20T09:00:00Z"
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val booking = BookingImpl(HttpClient(mockEngine), HttpClient(mockEngine))
            val result = booking.getContract(bookingId)
            assertEquals(42L, result.contractNumber)
            assertEquals("contract.docx", result.fileName)
            assertEquals("https://minio.example/contract.docx", result.downloadUrl)
        }

    @Test
    fun `BookingContractDownloadDto deserializes snake_case`() =
        runTest {
            val mockEngine =
                MockEngine {
                    respond(
                        content =
                            """
                            {
                              "contract_number": 1,
                              "fileName": "a.docx",
                              "download_url": "https://x",
                              "url_expires_at": "2026-01-01T00:00:00Z",
                              "generated_at": "2026-01-01T00:00:00Z"
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val response = HttpClient(mockEngine).get("https://test")
            val result: BookingContractDownloadDto = response.parseResponse()
            assertEquals(1L, result.contractNumber)
            assertEquals("https://x", result.downloadUrl)
        }
}
