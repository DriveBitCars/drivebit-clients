package my.drivebit.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FilterChipStyleTest {
    @Test
    fun `unselected filter chip keeps blue text with lighter gray border`() {
        val style = filterChipStyle(isSelected = false)
        assertEquals("white", style.background)
        assertEquals("gray", style.border)
        assertEquals("blue", style.text)
        assertEquals("blue", style.arrow)
        assertFalse(style.usesNeutralChrome)
        assertTrue(style.hasLighterBorder)
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

    @Test
    fun `reset chip uses solid theme blue fill with white text`() {
        val style = filtersResetChipStyle()
        assertEquals("blue", style.background)
        assertEquals("blue", style.border)
        assertEquals("white", style.text)
        assertEquals("blue-strong", style.hoverBackground)
        assertTrue(style.isFilled)
        assertTrue(style.isBrighterThanOutline)
    }
}
