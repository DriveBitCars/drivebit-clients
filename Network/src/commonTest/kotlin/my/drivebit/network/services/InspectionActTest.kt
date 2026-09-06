package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InspectionActTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

    @Test
    fun `inspection act decodes permissions photos and signing state`() {
        val act =
            json.decodeFromString(
                BookingInspectionActDto.serializer(),
                """
                {
                  "id":"11111111-1111-1111-1111-111111111111",
                  "bookingId":"22222222-2222-2222-2222-222222222222",
                  "type":"Handover",
                  "actNumber":42,
                  "fuelRemaining":75,
                  "mileage":120500,
                  "ownerComment":"Есть царапина на двери",
                  "renterComment":"Принял",
                  "signedByOwnerAt":"2026-08-29T09:00:00Z",
                  "signedByRenterAt":null,
                  "isSignedByOwner":true,
                  "isSignedByRenter":false,
                  "isFullySigned":false,
                  "hasPdf":false,
                  "status":"AwaitingRenter",
                  "canEditOwnerFields":false,
                  "canEditRenterFields":true,
                  "canSignAsOwner":false,
                  "canSignAsRenter":true,
                  "createdAt":"2026-08-29T08:00:00Z",
                  "updatedAt":"2026-08-29T09:00:00Z",
                  "photos":[
                    {
                      "id":"33333333-3333-3333-3333-333333333333",
                      "authorId":"44444444-4444-4444-4444-444444444444",
                      "authorName":"Владелец",
                      "kind":"Dashboard",
                      "fileName":"dashboard.jpg",
                      "url":"/publicbct/inspection/dashboard.jpg",
                      "uploadedAt":"2026-08-29T08:30:00Z"
                    }
                  ]
                }
                """.trimIndent(),
            )

        assertEquals(InspectionActType.Handover, act.type)
        assertEquals(InspectionActStatus.AwaitingRenter, act.status)
        assertEquals(75, act.fuelRemaining)
        assertEquals(120500, act.mileage)
        assertTrue(act.isSignedByOwner)
        assertFalse(act.isSignedByRenter)
        assertTrue(act.canEditRenterFields)
        assertTrue(act.canSignAsRenter)
        assertNull(act.signedByRenterAt)
        assertEquals(1, act.photos?.size)
        assertEquals(InspectionPhotoKind.Dashboard, act.photos?.single()?.kind)
    }

    @Test
    fun `inspection act download decodes nullable url`() {
        val download =
            json.decodeFromString(
                BookingInspectionActDownloadDto.serializer(),
                """
                {
                  "actId":"11111111-1111-1111-1111-111111111111",
                  "actNumber":42,
                  "type":"Return",
                  "fileName":null,
                  "downloadUrl":null,
                  "urlExpiresAt":"2026-08-29T10:00:00Z"
                }
                """.trimIndent(),
            )

        assertEquals(InspectionActType.Return, download.type)
        assertEquals(42L, download.actNumber)
        assertNull(download.fileName)
        assertNull(download.downloadUrl)
    }

    @Test
    fun `supported inspection act types contain handover and return in order`() {
        assertEquals(
            listOf(InspectionActType.Handover, InspectionActType.Return),
            supportedInspectionActTypes,
        )
    }

    @Test
    fun `inspection act photo page path encodes reserved query characters`() {
        val path = inspectionActPhotoPagePath("https://cdn.example/photo.jpg?x=1&y=2")
        assertTrue(path.startsWith("/inspection-act/photo?url="))
        assertFalse(path.contains("&y="))
        assertTrue(path.contains("y"))
    }

    @Test
    fun `inspection act photo path is distinct from act page`() {
        assertTrue(isInspectionActPhotoPath("/inspection-act/photo"))
        assertTrue(isInspectionActPhotoPath("/inspection-act/photo?url=abc"))
        assertFalse(isInspectionActPhotoPath("/inspection-act"))
        assertFalse(isInspectionActPhotoPath("/inspection-act?bookingId=1&type=Handover"))
    }

    @Test
    fun `get uses inspection act path`() =
        runTest {
            val request =
                assertRequest(
                    method = HttpMethod.Get,
                    path = "/Booking/booking-1/inspection-acts/Handover",
                )

            InspectionActImpl(request.client).get("booking-1", InspectionActType.Handover)
            request.verify()
        }

    @Test
    fun `openOrCreate uses post and return path`() =
        runTest {
            val request =
                assertRequest(
                    method = HttpMethod.Post,
                    path = "/Booking/booking-1/inspection-acts/Return",
                )

            InspectionActImpl(request.client).openOrCreate("booking-1", InspectionActType.Return)
            request.verify()
        }

    @Test
    fun `update metrics sends JSON body`() =
        runTest {
            val request =
                assertRequest(
                    method = HttpMethod.Put,
                    path = "/Booking/booking-1/inspection-acts/Handover/metrics",
                    responseBody = sampleActJson,
                )

            InspectionActImpl(request.client).updateMetrics(
                bookingId = "booking-1",
                type = InspectionActType.Handover,
                request = UpdateInspectionMetricsRequest(75, 120500),
            )

            request.verify()
            assertEquals(
                """{"fuelRemaining":75,"mileage":120500}""",
                (request.body as TextContent).text,
            )
        }

    @Test
    fun `update comment sends JSON body`() =
        runTest {
            val request =
                assertRequest(
                    method = HttpMethod.Put,
                    path = "/Booking/booking-1/inspection-acts/Return/comment",
                    responseBody = sampleActJson,
                )

            InspectionActImpl(request.client).updateComment(
                bookingId = "booking-1",
                type = InspectionActType.Return,
                request = UpdateInspectionCommentRequest("Принял"),
            )

            request.verify()
            assertEquals("""{"comment":"Принял"}""", (request.body as TextContent).text)
        }

    @Test
    fun `upload photo sends multipart body`() =
        runTest {
            val request =
                assertRequest(
                    method = HttpMethod.Post,
                    path = "/Booking/booking-1/inspection-acts/Return/photos",
                    responseBody =
                        """
                        {
                          "id":"33333333-3333-3333-3333-333333333333",
                          "authorId":"44444444-4444-4444-4444-444444444444",
                          "kind":"Car",
                          "uploadedAt":"2026-08-29T08:30:00Z"
                        }
                        """.trimIndent(),
                )

            InspectionActImpl(request.client).uploadPhoto(
                bookingId = "booking-1",
                type = InspectionActType.Return,
                fileBytes = byteArrayOf(1, 2, 3),
                fileName = "car.jpg",
                contentType = "image/jpeg",
                kind = InspectionPhotoKind.Car,
            )

            request.verify()
            assertTrue(request.body is MultiPartFormDataContent)
        }

    @Test
    fun `delete photo uses photo path`() =
        runTest {
            val request =
                assertRequest(
                    method = HttpMethod.Delete,
                    path = "/Booking/booking-1/inspection-acts/Return/photos/photo-1",
                    responseBody = """{"success":true}""",
                )

            InspectionActImpl(request.client).deletePhoto(
                "booking-1",
                InspectionActType.Return,
                "photo-1",
            )
            request.verify()
        }

    @Test
    fun `sign endpoints use matching party paths`() =
        runTest {
            val ownerRequest =
                assertRequest(
                    method = HttpMethod.Post,
                    path = "/Booking/booking-1/inspection-acts/Handover/sign-as-owner",
                    responseBody = sampleActJson,
                )
            InspectionActImpl(ownerRequest.client).signAsOwner("booking-1", InspectionActType.Handover)
            ownerRequest.verify()

            val renterRequest =
                assertRequest(
                    method = HttpMethod.Post,
                    path = "/Booking/booking-1/inspection-acts/Return/sign-as-renter",
                    responseBody = sampleActJson,
                )
            InspectionActImpl(renterRequest.client).signAsRenter("booking-1", InspectionActType.Return)
            renterRequest.verify()
        }

    @Test
    fun `download uses download path`() =
        runTest {
            val request =
                assertRequest(
                    method = HttpMethod.Get,
                    path = "/Booking/booking-1/inspection-acts/Handover/download",
                    responseBody =
                        """
                        {
                          "actId":"11111111-1111-1111-1111-111111111111",
                          "actNumber":42,
                          "type":"Handover",
                          "fileName":"act.pdf",
                          "downloadUrl":"https://example.com/act.pdf",
                          "urlExpiresAt":"2026-08-29T10:00:00Z"
                        }
                        """.trimIndent(),
                )

            val result = InspectionActImpl(request.client).download("booking-1", InspectionActType.Handover)

            request.verify()
            assertEquals("act.pdf", result.fileName)
            assertEquals("https://example.com/act.pdf", result.downloadUrl)
        }

    @Test
    fun `blank booking id fails before request`() =
        runTest {
            var called = false
            val client =
                HttpClient(
                    MockEngine {
                        called = true
                        respond(
                            content = sampleActJson,
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    },
                )

            val error =
                runCatching {
                    InspectionActImpl(client).get(" ", InspectionActType.Handover)
                }.exceptionOrNull()
            assertTrue(error is IllegalArgumentException)
            assertFalse(called)
        }

    private fun assertRequest(
        method: HttpMethod,
        path: String,
        responseBody: String = sampleActJson,
    ): CapturedRequest {
        val captured = CapturedRequest(method, path)
        captured.client =
            HttpClient(
                MockEngine { request ->
                    captured.method = request.method
                    captured.path = request.url.encodedPath
                    captured.body = request.body
                    respond(
                        content = responseBody,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                },
            ) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }

        return captured
    }

    private class CapturedRequest(
        val expectedMethod: HttpMethod,
        val expectedPath: String,
    ) {
        lateinit var client: HttpClient
        var method: HttpMethod? = null
        var path: String? = null
        var body: io.ktor.http.content.OutgoingContent? = null

        fun verify() {
            assertEquals(expectedMethod, method)
            assertTrue(path?.endsWith(expectedPath) == true)
        }
    }

    @Test
    fun `resolveInspectionActViewerRole maps owner and renter ids`() {
        assertEquals(
            InspectionActViewerRole.Owner,
            resolveInspectionActViewerRole("owner-1", "owner-1", "renter-1"),
        )
        assertEquals(
            InspectionActViewerRole.Renter,
            resolveInspectionActViewerRole("renter-1", "owner-1", "renter-1"),
        )
        assertEquals(
            null,
            resolveInspectionActViewerRole("stranger", "owner-1", "renter-1"),
        )
    }

    @Test
    fun `canCurrentUserEditMetrics follows edit flags for owner and renter`() {
        val act =
            BookingInspectionActDto(
                id = "act-1",
                bookingId = "booking-1",
                type = InspectionActType.Handover,
                actNumber = 1,
                status = InspectionActStatus.Draft,
                canEditOwnerFields = true,
                canEditRenterFields = true,
                createdAt = "2026-08-29T08:00:00Z",
                updatedAt = "2026-08-29T08:00:00Z",
            )

        assertTrue(act.canCurrentUserEditMetrics(InspectionActViewerRole.Owner))
        assertTrue(act.canCurrentUserEditMetrics(InspectionActViewerRole.Renter))
        assertFalse(
            act.copy(canEditOwnerFields = false).canCurrentUserEditMetrics(InspectionActViewerRole.Owner),
        )
        assertFalse(
            act.copy(canEditRenterFields = false).canCurrentUserEditMetrics(InspectionActViewerRole.Renter),
        )
    }

    @Test
    fun `canCurrentUserSign respects viewer role`() {
        val act =
            BookingInspectionActDto(
                id = "act-1",
                bookingId = "booking-1",
                type = InspectionActType.Handover,
                actNumber = 1,
                status = InspectionActStatus.Draft,
                canEditOwnerFields = true,
                canEditRenterFields = true,
                canSignAsOwner = true,
                canSignAsRenter = true,
                createdAt = "2026-08-29T08:00:00Z",
                updatedAt = "2026-08-29T08:00:00Z",
            )

        assertTrue(act.canCurrentUserSign(InspectionActViewerRole.Owner))
        assertTrue(act.canCurrentUserSign(InspectionActViewerRole.Renter))
        assertFalse(
            act
                .copy(
                    canEditOwnerFields = false,
                    canSignAsOwner = false,
                    isSignedByOwner = true,
                ).canCurrentUserSign(InspectionActViewerRole.Owner),
        )
    }

    @Test
    fun `owner can sign when metrics not yet saved on act`() {
        val act =
            BookingInspectionActDto(
                id = "act-1",
                bookingId = "booking-1",
                type = InspectionActType.Return,
                actNumber = 3,
                status = InspectionActStatus.AwaitingOwner,
                isSignedByRenter = true,
                canEditOwnerFields = true,
                canSignAsOwner = false,
                canSignAsRenter = false,
                createdAt = "2026-08-29T08:00:00Z",
                updatedAt = "2026-08-29T08:00:00Z",
            )

        assertTrue(act.canCurrentUserSign(InspectionActViewerRole.Owner))
        assertEquals(null, act.currentUserSignStatusMessage(InspectionActViewerRole.Owner))
    }

    @Test
    fun `sign status helpers show button or signed state for current user`() {
        val draft =
            BookingInspectionActDto(
                id = "act-1",
                bookingId = "booking-1",
                type = InspectionActType.Handover,
                actNumber = 1,
                status = InspectionActStatus.Draft,
                canEditOwnerFields = true,
                canSignAsOwner = true,
                isSignedByOwner = false,
                createdAt = "2026-08-29T08:00:00Z",
                updatedAt = "2026-08-29T08:00:00Z",
            )
        assertTrue(draft.canCurrentUserSign(InspectionActViewerRole.Owner))
        assertEquals(null, draft.currentUserSignStatusMessage(InspectionActViewerRole.Owner))

        val ownerSigned =
            draft.copy(
                isSignedByOwner = true,
                canEditOwnerFields = false,
                canSignAsOwner = false,
                status = InspectionActStatus.AwaitingRenter,
            )
        assertFalse(ownerSigned.canCurrentUserSign(InspectionActViewerRole.Owner))
        assertEquals("Вы подписали акт", ownerSigned.currentUserSignStatusMessage(InspectionActViewerRole.Owner))
        assertEquals(
            "Ожидаем подпись арендатора",
            ownerSigned.counterpartySignStatusMessage(InspectionActViewerRole.Owner),
        )

        val fullySigned =
            ownerSigned.copy(
                isSignedByRenter = true,
                isFullySigned = true,
                hasPdf = true,
                status = InspectionActStatus.SignedByBoth,
            )
        assertEquals(null, fullySigned.currentUserSignStatusMessage(InspectionActViewerRole.Owner))
        assertEquals(null, fullySigned.counterpartySignStatusMessage(InspectionActViewerRole.Owner))
    }

    private companion object {
        const val sampleActJson =
            """
            {
              "id":"11111111-1111-1111-1111-111111111111",
              "bookingId":"22222222-2222-2222-2222-222222222222",
              "type":"Handover",
              "actNumber":42,
              "status":"Draft",
              "createdAt":"2026-08-29T08:00:00Z",
              "updatedAt":"2026-08-29T08:00:00Z"
            }
            """
    }
}
