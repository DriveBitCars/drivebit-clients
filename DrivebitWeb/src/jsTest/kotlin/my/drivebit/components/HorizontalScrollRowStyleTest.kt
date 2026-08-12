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

    @Test
    fun `filter chips scrollbar is hidden while remaining scrollable`() {
        val style = horizontalScrollRowStyle()
        assertEquals("none", style.scrollbarWidth)
        assertEquals("none", style.msOverflowStyle)
        assertEquals("drivebit-horizontal-scroll", style.cssClass)
    }

    @Test
    fun `filter chips horizontal scroll matches home filters touch behavior`() {
        val style = horizontalScrollRowStyle()
        assertEquals("pan-x", style.touchAction)
        assertEquals("contain", style.overscrollBehaviorX)
        assertEquals("0", style.minWidth)
        assertEquals("drivebit-horizontal-scroll-track", style.trackCssClass)
    }
}
