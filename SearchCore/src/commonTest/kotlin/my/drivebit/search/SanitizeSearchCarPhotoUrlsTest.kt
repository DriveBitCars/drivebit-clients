package my.drivebit.search

import my.drivebit.network.services.CarAddress
import my.drivebit.network.services.CarGeneral
import my.drivebit.network.services.CarItem
import my.drivebit.network.services.CarPhotoItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SanitizeSearchCarPhotoUrlsTest {
    @Test
    fun sanitizeSearchCarPhotoUrls_rewritesDirectMinioHttpToPublicPath() {
        val raw =
            "http://157.22.252.70:9000/publicbct/cars/" +
                "bc71c63f-d244-4971-b194-9f4475b04c64/" +
                "e7c4e756-f699-47b4-8f2e-909b80831334_compressed.jpg"
        val car =
            CarItem(
                id = "1",
                year = 2020,
                price = 5000.0,
                photos =
                    listOf(
                        CarPhotoItem(id = 1, url = raw, uploadDate = "2026-01-01"),
                    ),
                general =
                    CarGeneral(
                        brandName = "BMW",
                        modelName = "X5",
                        seats = 5,
                        address = CarAddress(),
                        photos =
                            listOf(
                                CarPhotoItem(id = 1, url = raw, uploadDate = "2026-01-01"),
                            ),
                    ),
            )

        val sanitized = sanitizeSearchCarPhotoUrls(listOf(car)).single()

        assertEquals(
            "/publicbct/cars/bc71c63f-d244-4971-b194-9f4475b04c64/" +
                "e7c4e756-f699-47b4-8f2e-909b80831334_compressed.jpg",
            sanitized.photos.single().url,
        )
        assertEquals(sanitized.photos.single().url, sanitized.general.photos.single().url)
        assertFalse(sanitized.photos.single().url.contains("157.22.252.70"))
        assertFalse(sanitized.photos.single().url.startsWith("http"))
    }

    @Test
    fun sanitizeSearchCarPhotoUrls_rewritesThumbnailMinioHttpToPublicPath() {
        val rawThumb =
            "http://157.22.252.70:9000/publicbct/cars/" +
                "bc71c63f-d244-4971-b194-9f4475b04c64/" +
                "e7c4e756-f699-47b4-8f2e-909b80831334_compressed_thumbnail.jpg"
        val car =
            CarItem(
                id = "1",
                year = 2020,
                price = 5000.0,
                photos =
                    listOf(
                        CarPhotoItem(
                            id = 1,
                            url = "http://157.22.252.70:9000/publicbct/cars/x/full_compressed.jpg",
                            uploadDate = "2026-01-01",
                            thumbnailUrl = rawThumb,
                        ),
                    ),
                general =
                    CarGeneral(
                        brandName = "BMW",
                        modelName = "X5",
                        seats = 5,
                        address = CarAddress(),
                        photos =
                            listOf(
                                CarPhotoItem(
                                    id = 1,
                                    url = "http://157.22.252.70:9000/publicbct/cars/x/full_compressed.jpg",
                                    uploadDate = "2026-01-01",
                                    thumbnailUrl = rawThumb,
                                ),
                            ),
                    ),
            )

        val sanitized = sanitizeSearchCarPhotoUrls(listOf(car)).single()

        assertEquals(
            "/publicbct/cars/bc71c63f-d244-4971-b194-9f4475b04c64/" +
                "e7c4e756-f699-47b4-8f2e-909b80831334_compressed_thumbnail.jpg",
            sanitized.photos.single().thumbnailUrl,
        )
        assertEquals(
            sanitized.photos.single().thumbnailUrl,
            sanitized.general.photos.single().thumbnailUrl,
        )
        assertFalse(sanitized.photos.single().thumbnailUrl!!.startsWith("http"))
    }
}
