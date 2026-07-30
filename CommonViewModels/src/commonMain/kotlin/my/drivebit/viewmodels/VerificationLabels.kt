package my.drivebit.viewmodels

object VerificationLabels {
    const val VERIFIED_USER = "Проверенный пользователь"
    const val OWNER = "ПРОВЕРЕННЫЙ ВЛАДЕЛЕЦ"

    fun forUser(
        isPassportVerified: Boolean,
        isDriverLicenseVerified: Boolean,
    ): List<String> =
        if (isPassportVerified || isDriverLicenseVerified) {
            listOf(VERIFIED_USER)
        } else {
            emptyList()
        }

    fun forCarOwner(isOwnerVerified: Boolean): List<String> = if (isOwnerVerified) listOf(OWNER) else emptyList()

    @Suppress("UNUSED_PARAMETER")
    fun forCarSts(isStsVerified: Boolean): List<String> = emptyList()

    fun forBookingAsOwner(
        isRenterVerified: Boolean,
        isCarVerified: Boolean,
    ): List<String> =
        if (isRenterVerified || isCarVerified) {
            listOf(VERIFIED_USER)
        } else {
            emptyList()
        }

    fun forBookingAsRenter(
        isOwnerVerified: Boolean,
        isCarVerified: Boolean,
    ): List<String> =
        if (isOwnerVerified || isCarVerified) {
            listOf(VERIFIED_USER)
        } else {
            emptyList()
        }
}
