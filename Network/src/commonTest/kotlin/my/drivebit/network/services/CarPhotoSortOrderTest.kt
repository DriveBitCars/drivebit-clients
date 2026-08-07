package my.drivebit.network.services

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class CarPhotoSortOrderTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

    @Test
    fun `CarPhotoResponse decodes sortOrder`() {
        val photo =
            json.decodeFromString(
                CarPhotoResponse.serializer(),
                """{"id":1,"url":"https://cdn.example/a.jpg","uploadDate":"2026-01-01","sortOrder":3}""",
            )
        assertEquals(3, photo.sortOrder)
    }

    @Test
    fun `CarPhotoItem decodes sortOrder`() {
        val photo =
            json.decodeFromString(
                CarPhotoItem.serializer(),
                """{"id":1,"url":"https://cdn.example/a.jpg","uploadDate":"2026-01-01","sortOrder":2}""",
            )
        assertEquals(2, photo.sortOrder)
    }

    @Test
    fun `sortedBySortOrder orders CarPhotoResponse ascending`() {
        val photos =
            listOf(
                CarPhotoResponse(id = 1, url = "a", uploadDate = "d", sortOrder = 3),
                CarPhotoResponse(id = 2, url = "b", uploadDate = "d", sortOrder = 1),
                CarPhotoResponse(id = 3, url = "c", uploadDate = "d", sortOrder = 2),
            )
        assertEquals(listOf(2, 3, 1), photos.sortedBySortOrder().map { it.id })
    }

    @Test
    fun `sortedBySortOrder orders CarPhotoItem ascending`() {
        val photos =
            listOf(
                CarPhotoItem(id = 1, url = "a", uploadDate = "d", sortOrder = 2),
                CarPhotoItem(id = 2, url = "b", uploadDate = "d", sortOrder = 1),
            )
        assertEquals(listOf(2, 1), photos.sortedBySortOrder().map { it.id })
    }
}
