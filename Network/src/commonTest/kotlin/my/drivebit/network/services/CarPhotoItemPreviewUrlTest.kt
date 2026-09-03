package my.drivebit.network.services

import kotlin.test.Test
import kotlin.test.assertEquals

class CarPhotoItemPreviewUrlTest {
    @Test
    fun `previewUrl prefers non-blank thumbnailUrl`() {
        val photo =
            CarPhotoItem(
                id = 1,
                url = "https://cdn.example/full.jpg",
                uploadDate = "2026-01-01",
                thumbnailUrl = "https://cdn.example/thumb.jpg",
            )

        assertEquals("https://cdn.example/thumb.jpg", photo.previewUrl())
    }

    @Test
    fun `previewUrl falls back to url when thumbnailUrl is null`() {
        val photo =
            CarPhotoItem(
                id = 1,
                url = "https://cdn.example/full.jpg",
                uploadDate = "2026-01-01",
            )

        assertEquals("https://cdn.example/full.jpg", photo.previewUrl())
    }

    @Test
    fun `previewUrl falls back to url when thumbnailUrl is blank`() {
        val photo =
            CarPhotoItem(
                id = 1,
                url = "https://cdn.example/full.jpg",
                uploadDate = "2026-01-01",
                thumbnailUrl = "  ",
            )

        assertEquals("https://cdn.example/full.jpg", photo.previewUrl())
    }

    @Test
    fun `CarPhotoResponse previewUrl prefers thumbnailUrl`() {
        val photo =
            CarPhotoResponse(
                id = 1,
                url = "https://cdn.example/full.jpg",
                uploadDate = "2026-01-01",
                thumbnailUrl = "https://cdn.example/thumb.jpg",
            )

        assertEquals("https://cdn.example/thumb.jpg", photo.previewUrl())
    }

    @Test
    fun withSanitizedUrls_onPagesDev_prefixesProductionProxy() {
        val photo =
            CarPhotoItem(
                id = 1,
                url = "http://157.22.252.70:9000/publicbct/cars/abc/full.jpg",
                uploadDate = "2026-01-01",
                thumbnailUrl = "http://157.22.252.70:9000/publicbct/cars/abc/thumb.jpg",
            )

        val sanitized = photo.withSanitizedUrls("dev.drivebit.my")

        assertEquals("https://drivebit.ru/publicbct/cars/abc/full.jpg", sanitized.url)
        assertEquals("https://drivebit.ru/publicbct/cars/abc/thumb.jpg", sanitized.thumbnailUrl)
    }

    @Test
    fun withSanitizedUrls_onProduction_keepsRelativePublicbctPath() {
        val photo =
            CarPhotoItem(
                id = 1,
                url = "http://157.22.252.70:9000/publicbct/cars/abc/full.jpg",
                uploadDate = "2026-01-01",
                thumbnailUrl = "/publicbct/cars/abc/thumb.jpg",
            )

        val sanitized = photo.withSanitizedUrls("drivebit.ru")

        assertEquals("/publicbct/cars/abc/full.jpg", sanitized.url)
        assertEquals("/publicbct/cars/abc/thumb.jpg", sanitized.thumbnailUrl)
    }
}
