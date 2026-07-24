package my.drivebit.components

import kotlin.test.Test
import kotlin.test.assertEquals

class PaginationSummaryTest {
    @Test
    fun `empty filtered result shows database count`() {
        assertEquals(
            "Показано 0 из 0",
            paginationSummary(currentPage = 0, totalCount = 0, pageSize = 9),
        )
    }

    @Test
    fun `single page result shows complete database count`() {
        assertEquals(
            "Показано 1–2 из 2",
            paginationSummary(currentPage = 0, totalCount = 2, pageSize = 9),
        )
    }

    @Test
    fun `out of range page does not show inverted count`() {
        assertEquals(
            "Показано 0 из 2",
            paginationSummary(currentPage = 1, totalCount = 2, pageSize = 9),
        )
    }
}
