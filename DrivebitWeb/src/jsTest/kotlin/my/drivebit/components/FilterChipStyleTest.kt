package my.drivebit.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FilterChipStyleTest {
    @Test
    fun `unselected filter chip uses theme blue for text border and arrow`() {
        val style = filterChipStyle(isSelected = false)
        assertEquals("white", style.background)
        assertEquals("blue", style.border)
        assertEquals("blue", style.text)
        assertEquals("blue", style.arrow)
        assertFalse(style.usesNeutralChrome)
    }

    @Test
    fun `selected filter chip keeps white text on blue fill`() {
        val style = filterChipStyle(isSelected = true)
        assertEquals("blue", style.background)
        assertEquals("blue", style.border)
        assertEquals("white", style.text)
        assertEquals("white", style.arrow)
        assertTrue(style.isFilled)
    }
}
