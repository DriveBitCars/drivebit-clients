package my.drivebit.viewmodels

object VerificationLabels {
    const val PASSPORT = "ПАСПОРТ"
    const val DRIVER_LICENSE = "ВУ"
    const val STS = "СТС"
    const val OWNER = "ВЛАДЕЛЕЦ"
    const val RENTER = "АРЕНДАТОР"
    const val CAR = "АВТО"

    fun forUser(
        isPassportVerified: Boolean,
        isDriverLicenseVerified: Boolean,
    ): List<String> =
        buildList {
            if (isPassportVerified) add(PASSPORT)
            if (isDriverLicenseVerified) add(DRIVER_LICENSE)
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
