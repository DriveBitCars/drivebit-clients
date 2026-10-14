package my.drivebit.viewmodels

import kotlin.test.Test
import kotlin.test.assertEquals

class CarDescriptionLimitsTest {
    @Test
    fun `max length is 2000`() {
        assertEquals(2000, CarDescriptionLimits.MAX_LENGTH)
    }
}
