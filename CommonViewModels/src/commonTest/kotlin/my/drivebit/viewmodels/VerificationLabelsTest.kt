package my.drivebit.viewmodels

import kotlin.test.Test
import kotlin.test.assertEquals

class VerificationLabelsTest {
    @Test
    fun `forUser returns verified user when passport and driver license verified`() {
        assertEquals(
            listOf(VerificationLabels.VERIFIED_USER),
            VerificationLabels.forUser(isPassportVerified = true, isDriverLicenseVerified = true),
        )
        assertEquals("Проверенный пользователь", VerificationLabels.VERIFIED_USER)
    }

    @Test
    fun `forUser returns individual document labels when only one verified`() {
        assertEquals(
            listOf(VerificationLabels.PASSPORT),
            VerificationLabels.forUser(isPassportVerified = true, isDriverLicenseVerified = false),
        )
        assertEquals(
            listOf(VerificationLabels.DRIVER_LICENSE),
            VerificationLabels.forUser(isPassportVerified = false, isDriverLicenseVerified = true),
        )
        assertEquals(emptyList(), VerificationLabels.forUser(false, false))
    }

    @Test
    fun `forCarOwner keeps owner label unchanged`() {
        assertEquals(
            listOf(VerificationLabels.OWNER),
            VerificationLabels.forCarOwner(isOwnerVerified = true),
        )
        assertEquals("ПРОВЕРЕННЫЙ ВЛАДЕЛЕЦ", VerificationLabels.OWNER)
        assertEquals(emptyList(), VerificationLabels.forCarOwner(isOwnerVerified = false))
    }

    @Test
    fun `forBookingAsRenter returns owner and car labels`() {
        assertEquals(
            listOf(VerificationLabels.OWNER, VerificationLabels.CAR),
            VerificationLabels.forBookingAsRenter(isOwnerVerified = true, isCarVerified = true),
        )
        assertEquals("СТС", VerificationLabels.CAR)
        assertEquals("ПРОВЕРЕННЫЙ ВЛАДЕЛЕЦ", VerificationLabels.OWNER)
    }

    @Test
    fun `forBookingAsOwner returns renter and car labels`() {
        assertEquals(
            listOf(VerificationLabels.RENTER),
            VerificationLabels.forBookingAsOwner(isRenterVerified = true, isCarVerified = false),
        )
    }
}
