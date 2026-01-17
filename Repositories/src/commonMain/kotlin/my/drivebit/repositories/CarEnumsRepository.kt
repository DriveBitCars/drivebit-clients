package my.drivebit.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.drivebit.network.services.CarEnumsResponse
import my.drivebit.network.services.Dictionary

class CarEnumsLoadException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

interface CarEnumsRepository {
    suspend fun getEnums(): CarEnumsResponse

    suspend fun getColorByName(name: String): EnumItem

    suspend fun getBodyTypeByName(name: String): EnumItem

    suspend fun getStatusByName(name: String): EnumItem

    suspend fun getEngineTypeByName(name: String): EnumItem

    suspend fun getTransmissionTypeByName(name: String): EnumItem

    suspend fun getDriveTypeByName(name: String): EnumItem

    suspend fun getAllColors(): List<EnumItem>

    suspend fun getAllBodyTypes(): List<EnumItem>

    suspend fun getAllStatuses(): List<EnumItem>

    suspend fun getAllEngineTypes(): List<EnumItem>

    suspend fun getAllTransmissionTypes(): List<EnumItem>

    suspend fun getAllDriveTypes(): List<EnumItem>

    suspend fun getAllDocumentTypes(): List<EnumItem>
}

data class EnumItem(
    val number: Int,
    val name: String,
    val translate: String,
)

class CarEnumsRepositoryImpl(
    private val dictionary: Dictionary,
) : CarEnumsRepository {
    override suspend fun getEnums(): CarEnumsResponse =
        withContext(Dispatchers.Default) {
            runCatching {
                val enums = dictionary.getCarEnums()
                validateEnums(enums)
                enums
            }.getOrElse { e ->
                throw CarEnumsLoadException(
                    "Failed to load car enums from server: ${e.message}",
                    e,
                )
            }
        }

    override suspend fun getColorByName(name: String): EnumItem {
        val enums = getEnums()
        return enums.CarColorEnum
            .find { it.name == name }
            ?.toEnumItem()
            ?: throw CarEnumsLoadException("Color with name $name not found")
    }

    override suspend fun getBodyTypeByName(name: String): EnumItem {
        val enums = getEnums()
        return enums.BodyTypeEnum
            .find { it.name == name }
            ?.toEnumItem()
            ?: throw CarEnumsLoadException("BodyType with name $name not found")
    }

    override suspend fun getStatusByName(name: String): EnumItem {
        val enums = getEnums()
        return enums.CarStatusEnum
            .find { it.name == name }
            ?.toEnumItem()
            ?: throw CarEnumsLoadException("Status with name $name not found")
    }

    override suspend fun getEngineTypeByName(name: String): EnumItem {
        val enums = getEnums()
        return enums.EngineTypeEnum
            .find { it.name == name }
            ?.toEnumItem()
            ?: throw CarEnumsLoadException("EngineType with name $name not found")
    }

    override suspend fun getTransmissionTypeByName(name: String): EnumItem {
        val enums = getEnums()
        return enums.TransmissionTypeEnum
            .find { it.name == name }
            ?.toEnumItem()
            ?: throw CarEnumsLoadException("TransmissionType with name $name not found")
    }

    override suspend fun getDriveTypeByName(name: String): EnumItem {
        val enums = getEnums()
        return enums.DriveTypeEnum
            .find { it.name == name }
            ?.toEnumItem()
            ?: throw CarEnumsLoadException("DriveType with name $name not found")
    }

    override suspend fun getAllColors(): List<EnumItem> {
        val enums = getEnums()
        return enums.CarColorEnum.map { it.toEnumItem() }
    }

    override suspend fun getAllBodyTypes(): List<EnumItem> {
        val enums = getEnums()
        return enums.BodyTypeEnum.map { it.toEnumItem() }
    }

    override suspend fun getAllStatuses(): List<EnumItem> {
        val enums = getEnums()
        return enums.CarStatusEnum.map { it.toEnumItem() }
    }

    override suspend fun getAllEngineTypes(): List<EnumItem> {
        val enums = getEnums()
        return enums.EngineTypeEnum.map { it.toEnumItem() }
    }

    override suspend fun getAllTransmissionTypes(): List<EnumItem> {
        val enums = getEnums()
        return enums.TransmissionTypeEnum.map { it.toEnumItem() }
    }

    override suspend fun getAllDriveTypes(): List<EnumItem> {
        val enums = getEnums()
        return enums.DriveTypeEnum.map { it.toEnumItem() }
    }

    override suspend fun getAllDocumentTypes(): List<EnumItem> =
        withContext(Dispatchers.Default) {
            runCatching {
                val documentEnums = dictionary.getDocumentEnums()
                documentEnums.DocumentTypeEnum.map { it.toEnumItem() }
            }.getOrElse { e ->
                throw CarEnumsLoadException(
                    "Failed to load document enums from server: ${e.message}",
                    e,
                )
            }
        }

    private fun validateEnums(enums: CarEnumsResponse) {
        if (enums.CarColorEnum.isEmpty()) {
            throw CarEnumsLoadException("CarColorEnum is empty")
        }
        if (enums.BodyTypeEnum.isEmpty()) {
            throw CarEnumsLoadException("BodyTypeEnum is empty")
        }
        if (enums.CarStatusEnum.isEmpty()) {
            throw CarEnumsLoadException("CarStatusEnum is empty")
        }
        if (enums.EngineTypeEnum.isEmpty()) {
            throw CarEnumsLoadException("EngineTypeEnum is empty")
        }
        if (enums.TransmissionTypeEnum.isEmpty()) {
            throw CarEnumsLoadException("TransmissionTypeEnum is empty")
        }
        if (enums.DriveTypeEnum.isEmpty()) {
            throw CarEnumsLoadException("DriveTypeEnum is empty")
        }
        if (enums.SteeringWheelSideEnum.isEmpty()) {
            throw CarEnumsLoadException("SteeringWheelSideEnum is empty")
        }
        if (enums.SeatsHeatingEnum.isEmpty()) {
            throw CarEnumsLoadException("SeatsHeatingEnum is empty")
        }
        if (enums.SeatsVentilationEnum.isEmpty()) {
            throw CarEnumsLoadException("SeatsVentilationEnum is empty")
        }
        if (enums.SeatsMassageEnum.isEmpty()) {
            throw CarEnumsLoadException("SeatsMassageEnum is empty")
        }
        if (enums.ClimateControlEnum.isEmpty()) {
            throw CarEnumsLoadException("ClimateControlEnum is empty")
        }
        if (enums.DriveAssistantsEnum.isEmpty()) {
            throw CarEnumsLoadException("DriveAssistantsEnum is empty")
        }
        if (enums.AlarmSystemEnum.isEmpty()) {
            throw CarEnumsLoadException("AlarmSystemEnum is empty")
        }
        if (enums.MultimediaSystemEnum.isEmpty()) {
            throw CarEnumsLoadException("MultimediaSystemEnum is empty")
        }
        if (enums.CarRoofTypeEnum.isEmpty()) {
            throw CarEnumsLoadException("CarRoofTypeEnum is empty")
        }
        if (enums.MultimediaSystemOptionsEnum.isEmpty()) {
            throw CarEnumsLoadException("MultimediaSystemOptionsEnum is empty")
        }
        if (enums.ParkingAssistancesEnum.isEmpty()) {
            throw CarEnumsLoadException("ParkingAssistancesEnum is empty")
        }
    }
}

private fun my.drivebit.network.services.EnumItem.toEnumItem(): EnumItem =
    EnumItem(
        number = number,
        name = name,
        translate = translate,
    )
