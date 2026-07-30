package my.drivebit.viewmodels

import kotlin.test.Test
import kotlin.test.assertEquals

class VerificationLabelsTest {
    @Test
    fun `forUser returns verified user when passport verified`() {
        assertEquals(
            listOf(VerificationLabels.VERIFIED_USER),
            VerificationLabels.forUser(isPassportVerified = true, isDriverLicenseVerified = false),
        )
        assertEquals(
            listOf(VerificationLabels.VERIFIED_USER),
            VerificationLabels.forUser(isPassportVerified = true, isDriverLicenseVerified = true),
        )
        assertEquals("Проверенный пользователь", VerificationLabels.VERIFIED_USER)
    }

    @Test
    fun `forUser returns verified user when only driver license verified`() {
        assertEquals(
            listOf(VerificationLabels.VERIFIED_USER),
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
    fun `forCarSts never returns STS label`() {
        assertEquals(emptyList(), VerificationLabels.forCarSts(isStsVerified = true))
        assertEquals(emptyList(), VerificationLabels.forCarSts(isStsVerified = false))
    }

    @Test
    fun `forBookingAsRenter returns verified user when any document verified`() {
        assertEquals(
            listOf(VerificationLabels.VERIFIED_USER),
            VerificationLabels.forBookingAsRenter(isOwnerVerified = true, isCarVerified = true),
        )
        assertEquals(
            listOf(VerificationLabels.VERIFIED_USER),
            VerificationLabels.forBookingAsRenter(isOwnerVerified = true, isCarVerified = false),
        )
        assertEquals(
            listOf(VerificationLabels.VERIFIED_USER),
            VerificationLabels.forBookingAsRenter(isOwnerVerified = false, isCarVerified = true),
        )
        assertEquals(
            emptyList(),
            VerificationLabels.forBookingAsRenter(isOwnerVerified = false, isCarVerified = false),
        )
    }

    @Test
    fun `forBookingAsOwner returns verified user when any document verified`() {
        assertEquals(
            listOf(VerificationLabels.VERIFIED_USER),
            VerificationLabels.forBookingAsOwner(isRenterVerified = true, isCarVerified = false),
        )
        assertEquals(
            listOf(VerificationLabels.VERIFIED_USER),
            VerificationLabels.forBookingAsOwner(isRenterVerified = false, isCarVerified = true),
        )
        assertEquals(
            listOf(VerificationLabels.VERIFIED_USER),
            VerificationLabels.forBookingAsOwner(isRenterVerified = true, isCarVerified = true),
        )
        assertEquals(
            emptyList(),
            VerificationLabels.forBookingAsOwner(isRenterVerified = false, isCarVerified = false),
        )
    }
}
