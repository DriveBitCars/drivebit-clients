package my.drivebit.viewmodels

import kotlin.test.Test
import kotlin.test.assertEquals

class VerificationLabelsTest {
    @Test
    fun `forUser returns passport and driver license labels`() {
        assertEquals(
            listOf(VerificationLabels.PASSPORT, VerificationLabels.DRIVER_LICENSE),
            VerificationLabels.forUser(isPassportVerified = true, isDriverLicenseVerified = true),
        )
        assertEquals(emptyList(), VerificationLabels.forUser(false, false))
    }

    @Test
    fun `forBookingAsRenter returns owner and car labels`() {
        assertEquals(
            listOf(VerificationLabels.OWNER, VerificationLabels.CAR),
            VerificationLabels.forBookingAsRenter(isOwnerVerified = true, isCarVerified = true),
        )
    }

    @Test
    fun `forBookingAsOwner returns renter and car labels`() {
        assertEquals(
            listOf(VerificationLabels.RENTER),
            VerificationLabels.forBookingAsOwner(isRenterVerified = true, isCarVerified = false),
        )
    }
}
