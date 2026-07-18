package my.drivebit.search

import kotlin.test.Test
import kotlin.test.assertEquals

class ApplySearchFilterChangeTest {
    @Test
    fun `filter change resets page to 1`() {
        val current =
            SearchFilterSet(
                citySlug = "moskva",
                seatsMin = 2,
                page = 3,
            )
        val updated =
            applySearchFilterChange(current) { it.copy(seatsMin = 5) }
        assertEquals(5, updated.seatsMin)
        assertEquals(1, updated.page)
        assertEquals("moskva", updated.citySlug)
    }

    @Test
    fun `filter change resets page even when transform sets page`() {
        val current =
            SearchFilterSet(
                citySlug = "moskva",
                page = 3,
            )
        val updated =
            applySearchFilterChange(current) { it.copy(seatsMin = 5, page = 99) }
        assertEquals(5, updated.seatsMin)
        assertEquals(1, updated.page)
    }

    @Test
    fun `next page uses copy not filter-change helper`() {
        val current =
            SearchFilterSet(
                citySlug = "moskva",
                seatsMin = 5,
                page = 1,
            )
        val next = current.copy(page = 2)
        assertEquals(2, next.page)
        assertEquals(5, next.seatsMin)
        assertEquals("moskva", next.citySlug)
    }
}
