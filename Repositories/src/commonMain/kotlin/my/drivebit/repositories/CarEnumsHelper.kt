package my.drivebit.repositories

class CarEnumsHelper(
    private val repository: CarEnumsRepository,
) {
    suspend fun getColorTranslate(name: String): String = repository.getColorByName(name).translate

    suspend fun getBodyTypeTranslate(name: String): String = repository.getBodyTypeByName(name).translate

    suspend fun getStatusTranslate(name: String): String = repository.getStatusByName(name).translate

    suspend fun getEngineTypeTranslate(name: String): String = repository.getEngineTypeByName(name).translate

    suspend fun getTransmissionTypeTranslate(name: String): String =
        repository.getTransmissionTypeByName(name).translate

    suspend fun getDriveTypeTranslate(name: String): String = repository.getDriveTypeByName(name).translate

    suspend fun getTrunkSizeTranslate(name: String): String = repository.getTrunkSizeByName(name).translate

    suspend fun getAllColors(useTranslation: Boolean = true): List<EnumItem> = repository.getAllColors()

    suspend fun getAllBodyTypes(useTranslation: Boolean = true): List<EnumItem> = repository.getAllBodyTypes()

    suspend fun getAllStatuses(useTranslation: Boolean = true): List<EnumItem> = repository.getAllStatuses()

    suspend fun getAllEngineTypes(useTranslation: Boolean = true): List<EnumItem> = repository.getAllEngineTypes()

    suspend fun getAllTransmissionTypes(useTranslation: Boolean = true): List<EnumItem> =
        repository.getAllTransmissionTypes()

    suspend fun getAllDriveTypes(useTranslation: Boolean = true): List<EnumItem> = repository.getAllDriveTypes()

    suspend fun getAllTrunkSizes(useTranslation: Boolean = true): List<EnumItem> = repository.getAllTrunkSizes()

    suspend fun getAllTravelDestinations(useTranslation: Boolean = true): List<EnumItem> =
        repository.getAllTravelDestinations()

    suspend fun refreshEnums() {
        repository.getEnums()
    }
}
