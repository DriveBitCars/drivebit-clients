package my.drivebit.network.services

object DocumentUploadType {
    const val PassportMainPageRus = "PassportMainPageRus"
    const val PassportSecondaryPageRus = "PassportSecondaryPageRus"
    const val DriverLicense = "DriverLicense"
    const val DriverLicenseBack = "DriverLicenseBack"
    const val VehicleRegistrationFrontRus = "VehicleRegistrationFrontRus"
    const val VehicleRegistrationBackRus = "VehicleRegistrationBackRus"

    fun uploadPath(documentType: String): String =
        when (documentType) {
            PassportMainPageRus -> "upload/passport-main"
            PassportSecondaryPageRus -> "upload/passport-secondary"
            DriverLicense -> "upload/driver-license"
            DriverLicenseBack -> "upload/driver-license-back"
            VehicleRegistrationFrontRus -> "upload/sts-front"
            VehicleRegistrationBackRus -> "upload/sts-back"
            else -> error("Unsupported document type: $documentType")
        }

    fun requiresCarId(documentType: String): Boolean =
        documentType == VehicleRegistrationFrontRus || documentType == VehicleRegistrationBackRus
}
