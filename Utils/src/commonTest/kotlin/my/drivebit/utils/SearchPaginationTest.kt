package my.drivebit.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class SearchPaginationTest {
    @Test
    fun `url page 1 maps to ui index 0`() {
        assertEquals(0, urlPageToUiIndex(1))
        assertEquals(0, urlPageToUiIndex(0))
        assertEquals(0, urlPageToUiIndex(-3))
    }

    @Test
    fun `url page N maps to ui index N-1`() {
        assertEquals(1, urlPageToUiIndex(2))
        assertEquals(4, urlPageToUiIndex(5))
    }

    @Test
    fun `ui index maps back to url page`() {
        assertEquals(1, uiIndexToUrlPage(0))
        assertEquals(3, uiIndexToUrlPage(2))
        assertEquals(1, uiIndexToUrlPage(-1))
    }
}
