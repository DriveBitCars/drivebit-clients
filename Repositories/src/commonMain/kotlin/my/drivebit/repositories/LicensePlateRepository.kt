package my.drivebit.repositories

import com.russhwolf.settings.Settings

interface LicensePlateRepository {
    fun saveLicensePlate(licensePlate: String)

    fun getLicensePlate(): String?

    fun clearLicensePlate()
}

internal class LicensePlateRepositoryImpl(
    private val settings: Settings,
) : LicensePlateRepository {
    companion object {
        private const val LICENSE_PLATE_KEY = "license_plate"
    }

    override fun saveLicensePlate(licensePlate: String) {
        settings.putString(LICENSE_PLATE_KEY, licensePlate)
    }

    override fun getLicensePlate(): String? = settings.getStringOrNullIfEmpty(LICENSE_PLATE_KEY)

    override fun clearLicensePlate() {
        settings.remove(LICENSE_PLATE_KEY)
    }
}
