package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TelegramNotificationsTest {
    @Test
    fun `getLinkStatus parses isLinked false`() =
        runTest {
            val engine =
                MockEngine { request ->
                    assertEquals(HttpMethod.Get, request.method)
                    assertTrue(request.url.encodedPath.endsWith("/Notifications/Telegram/link/status"))
                    respond(
                        content = """{"isLinked":false}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val api = TelegramNotificationsImpl(HttpClient(engine))
            val status = api.getLinkStatus()
            assertFalse(status.isLinked)
        }

    @Test
    fun `createLink parses code and deepLinkUrl`() =
        runTest {
            val engine =
                MockEngine { request ->
                    assertEquals(HttpMethod.Post, request.method)
                    assertTrue(request.url.encodedPath.endsWith("/Notifications/Telegram/link"))
                    respond(
                        content =
                            """
                            {
                              "token":"tok",
                              "code":"489366",
                              "deepLinkUrl":"https://t.me/drivebit_bot?start=tok",
                              "expiresAt":"2026-07-20T12:00:00Z"
                            }
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val api = TelegramNotificationsImpl(HttpClient(engine))
            val link = api.createLink()
            assertEquals("489366", link.code)
            assertEquals("https://t.me/drivebit_bot?start=tok", link.deepLinkUrl)
            assertEquals("tok", link.token)
        }

    @Test
    fun `unlink succeeds on HTTP 204`() =
        runTest {
            var called = false
            val engine =
                MockEngine { request ->
                    called = true
                    assertEquals(HttpMethod.Post, request.method)
                    assertTrue(request.url.encodedPath.endsWith("/Notifications/Telegram/unlink"))
                    respond(
                        content = "",
                        status = HttpStatusCode.NoContent,
                    )
                }
            val api = TelegramNotificationsImpl(HttpClient(engine))
            api.unlink()
            assertTrue(called)
        }
}
