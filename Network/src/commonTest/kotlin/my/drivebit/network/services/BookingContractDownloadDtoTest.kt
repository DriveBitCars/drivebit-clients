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
import kotlin.test.assertIs

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
            val success = assertIs<GetBookingContractResult.Success>(result)
            assertEquals(42L, success.contract.contractNumber)
            assertEquals("contract.docx", success.contract.fileName)
            assertEquals("https://minio.example/contract.docx", success.contract.downloadUrl)
        }

    @Test
    fun `getContract returns DataIncomplete when 422`() =
        runTest {
            val bookingId = "550e8400-e29b-41d4-a716-446655440000"
            val mockEngine =
                MockEngine {
                    respond(
                        content =
                            """
                            {
                              "errorCode": "CONTRACT_DATA_INCOMPLETE",
                              "message": "Невозможно скачать договор: не заполнены обязательные данные.",
                              "reasons": [
                                "Не заполнены паспортные данные арендатора.",
                                "Не указаны данные СТС автомобиля."
                              ]
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.UnprocessableEntity,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val booking = BookingImpl(HttpClient(mockEngine), HttpClient(mockEngine))
            val result = booking.getContract(bookingId)
            val incomplete = assertIs<GetBookingContractResult.DataIncomplete>(result)
            assertEquals(2, incomplete.reasons.size)
            assertEquals("Не заполнены паспортные данные арендатора.", incomplete.reasons[0])
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

    @Test
    fun `browserDownloadUrl rewrites direct MinIO presigned URL to same-origin path`() {
        val dto =
            BookingContractDownloadDto(
                contractNumber = 1L,
                fileName = "dogovor.pdf",
                downloadUrl =
                    "http://157.22.252.70:9000/privatebct/contracts/booking/file.pdf" +
                        "?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Signature=abc",
                urlExpiresAt = "2026-01-01T00:00:00Z",
                generatedAt = "2026-01-01T00:00:00Z",
            )

        assertEquals(
            "/privatebct/contracts/booking/file.pdf?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Signature=abc",
            dto.browserDownloadUrl(),
        )
    }

    @Test
    fun `browserDownloadUrl leaves already proxied URLs unchanged`() {
        val dto =
            BookingContractDownloadDto(
                contractNumber = 1L,
                fileName = "dogovor.pdf",
                downloadUrl = "https://drivebit.ru/privatebct/contracts/file.pdf?sig=1",
                urlExpiresAt = "2026-01-01T00:00:00Z",
                generatedAt = "2026-01-01T00:00:00Z",
            )

        assertEquals(dto.downloadUrl, dto.browserDownloadUrl())
    }
}
