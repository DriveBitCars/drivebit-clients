package my.drivebit.network

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HttpClientFactoryTest {
    @Test
    fun `DEFAULT_BASE_URL should be valid URL`() {
        assertTrue(
            DEFAULT_BASE_URL.startsWith("http://") || DEFAULT_BASE_URL.startsWith("https://"),
            "Base URL should start with http:// or https://",
        )
    }

    @Test
    fun `createHttpClientWithConfig should create HttpClient with default base URL`() {
        val httpClient = createHttpClientWithConfig()
        assertNotNull(httpClient, "HttpClient should not be null")
    }
}
