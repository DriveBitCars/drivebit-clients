package my.drivebit.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class HorizontalScrollRowStyleTest {
    @Test
    fun `filter chips row stays on one line and scrolls horizontally`() {
        val style = horizontalScrollRowStyle()
        assertEquals("nowrap", style.flexWrap)
        assertEquals("auto", style.overflowX)
        assertFalse(style.wraps)
    }

    @Test
    fun `filter chips do not shrink inside scroll row`() {
        assertEquals("0", horizontalScrollRowStyle().childFlexShrink)
    }
}
