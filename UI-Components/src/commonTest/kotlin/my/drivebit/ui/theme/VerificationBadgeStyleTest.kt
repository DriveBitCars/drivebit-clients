package my.drivebit.ui.theme

import kotlin.test.Test
import kotlin.test.assertEquals

class VerificationBadgeStyleTest {
    @Test
    fun `uses brand blue for content and border`() {
        assertEquals(ColorsDriveBit.Blue, VerificationBadgeStyle.contentColor)
        assertEquals(ColorsDriveBit.Blue, VerificationBadgeStyle.borderColor)
        assertEquals(1.5f, VerificationBadgeStyle.borderWidthDp)
    }
}
