package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DocumentsUploadTest {
    @Test
    fun uploadPath_mapsKnownDocumentTypes() {
        assertEquals("upload/passport-main", DocumentUploadType.uploadPath(DocumentUploadType.PassportMainPageRus))
        assertEquals("upload/driver-license-back", DocumentUploadType.uploadPath(DocumentUploadType.DriverLicenseBack))
        assertEquals("upload/sts-front", DocumentUploadType.uploadPath(DocumentUploadType.VehicleRegistrationFrontRus))
    }

    @Test
    fun `uploadDocument posts to typed driver-license-back endpoint`() =
        runTest {
            val mockEngine =
                MockEngine { request ->
                    assertTrue(request.url.encodedPath.endsWith("/Documents/upload/driver-license-back"))
                    respond(
                        content =
                            """
                            {
                              "id": 7,
                              "type": "DriverLicenseBack",
                              "status": "Pending"
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val documents = DocumentsImpl(HttpClient(mockEngine))
            val uploaded =
                documents.uploadDocument(
                    fileBytes = byteArrayOf(1, 2, 3),
                    fileName = "license-back.jpg",
                    contentType = "image/jpeg",
                    documentType = DocumentUploadType.DriverLicenseBack,
                )
            assertEquals(7, uploaded.id)
            assertEquals("DriverLicenseBack", uploaded.type)
        }

    @Test
    fun `uploadDocument posts sts-front with carId`() =
        runTest {
            val mockEngine =
                MockEngine { request ->
                    assertTrue(request.url.encodedPath.endsWith("/Documents/upload/sts-front"))
                    respond(
                        content =
                            """
                            {
                              "id": 8,
                              "type": "VehicleRegistrationFrontRus",
                              "carId": "car-123",
                              "status": "Pending"
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val documents = DocumentsImpl(HttpClient(mockEngine))
            val uploaded =
                documents.uploadDocument(
                    fileBytes = byteArrayOf(9),
                    fileName = "sts.jpg",
                    contentType = "image/jpeg",
                    documentType = DocumentUploadType.VehicleRegistrationFrontRus,
                    carId = "car-123",
                )
            assertEquals("car-123", uploaded.carId)
        }

    @Test
    fun `uploadDocument requires carId for sts documents`() =
        runTest {
            val documents = DocumentsImpl(HttpClient(MockEngine { respond("{}", HttpStatusCode.OK) }))
            assertFailsWith<IllegalStateException> {
                documents.uploadDocument(
                    fileBytes = byteArrayOf(1),
                    fileName = "sts.jpg",
                    contentType = "image/jpeg",
                    documentType = DocumentUploadType.VehicleRegistrationFrontRus,
                    carId = null,
                )
            }
        }
}
