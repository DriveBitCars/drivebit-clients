package my.drivebit.viewmodels

object VerificationLabels {
    const val DRIVER_LICENSE = "ВУ"
    const val STS = "СТС"
    const val VERIFIED_USER = "Проверенный пользователь"
    const val OWNER = "ПРОВЕРЕННЫЙ ВЛАДЕЛЕЦ"
    const val RENTER = "АРЕНДАТОР"
    const val CAR = "СТС"

    fun forUser(
        isPassportVerified: Boolean,
        isDriverLicenseVerified: Boolean,
    ): List<String> =
        when {
            isPassportVerified -> listOf(VERIFIED_USER)
            isDriverLicenseVerified -> listOf(DRIVER_LICENSE)
            else -> emptyList()
        }

    fun forCarOwner(isOwnerVerified: Boolean): List<String> = if (isOwnerVerified) listOf(OWNER) else emptyList()

    fun forCarSts(isStsVerified: Boolean): List<String> = if (isStsVerified) listOf(STS) else emptyList()

    fun forBookingAsOwner(
        isRenterVerified: Boolean,
        isCarVerified: Boolean,
    ): List<String> =
        buildList {
            if (isRenterVerified) add(RENTER)
            if (isCarVerified) add(CAR)
        }

    fun forBookingAsRenter(
        isOwnerVerified: Boolean,
        isCarVerified: Boolean,
    ): List<String> =
        buildList {
            if (isOwnerVerified) add(OWNER)
            if (isCarVerified) add(CAR)
        }
}
