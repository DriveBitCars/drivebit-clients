package my.drivebit.network.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class UrlSanitizerTest {
    @Test
    fun `MinIO URL should be converted to HTTPS URL`() {
        val input = "http://155.212.170.94:9000/publicbct/cars/a575c0b1-3736-475f-a4a8-5a87cfbdb18a/c876fa26-2a58-4ec2-8b3a-82c84d0fef53_compressed.jpg"
        val expected = "https://drivebit.my/publicbct/cars/a575c0b1-3736-475f-a4a8-5a87cfbdb18a/c876fa26-2a58-4ec2-8b3a-82c84d0fef53_compressed.jpg"
        val result = UrlSanitizer.ensureHttpsUrl(input)
        assertEquals(expected, result)
    }

    @Test
    fun `MinIO URL with https should be converted`() {
        val input = "https://155.212.170.94:9000/publicbct/cars/a575c0b1-3736-475f-a4a8-5a87cfbdb18a/c876fa26-2a58-4ec2-8b3a-82c84d0fef53_compressed.jpg"
        val expected = "https://drivebit.my/publicbct/cars/a575c0b1-3736-475f-a4a8-5a87cfbdb18a/c876fa26-2a58-4ec2-8b3a-82c84d0fef53_compressed.jpg"
        val result = UrlSanitizer.ensureHttpsUrl(input)
        assertEquals(expected, result)
    }
}
