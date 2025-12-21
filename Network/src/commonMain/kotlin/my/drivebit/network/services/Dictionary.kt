package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable
import my.drivebit.network.DEFAULT_BASE_URL
import my.drivebit.network.parseResponse

interface Dictionary {
    suspend fun getCarBrands(): List<CarBrand>

    suspend fun getCarModels(brandId: Int): List<CarModel>

    suspend fun searchCities(query: String): List<City>

    suspend fun getCarEnums(): CarEnumsResponse
}

@Serializable
data class CarBrand(
    val id: Int,
    val apiId: String? = null,
    val name: String,
    val cyrillicName: String? = null,
    val country: String? = null,
)

@Serializable
data class CarModel(
    val id: Int,
    val brandId: Int,
    val name: String,
    val cyrillicName: String? = null,
)

@Serializable
data class City(
    val id: Int,
    val name: String,
)

@Serializable
data class CarEnumsResponse(
    val CarColorEnum: List<EnumItem>,
    val BodyTypeEnum: List<EnumItem>,
    val CarStatusEnum: List<EnumItem>,
    val EngineTypeEnum: List<EnumItem>,
    val TransmissionTypeEnum: List<EnumItem>,
    val DriveTypeEnum: List<EnumItem>,
    val SteeringWheelSideEnum: List<EnumItem>,
    val SeatsHeatingEnum: List<EnumItem>,
    val SeatsVentilationEnum: List<EnumItem>,
    val SeatsMassageEnum: List<EnumItem>,
    val ClimateControlEnum: List<EnumItem>,
    val DriveAssistantsEnum: List<EnumItem>,
    val AlarmSystemEnum: List<EnumItem>,
    val MultimediaSystemEnum: List<EnumItem>,
    val CarRoofTypeEnum: List<EnumItem>,
    val MultimediaSystemOptionsEnum: List<EnumItem>,
    val ParkingAssistancesEnum: List<EnumItem>,
)

@Serializable
data class EnumItem(
    val number: Int,
    val name: String,
    val translate: String,
)

class DictionaryImpl(
    private val httpClient: HttpClient,
) : Dictionary {
    override suspend fun getCarBrands(): List<CarBrand> {
        val url = "${DEFAULT_BASE_URL}Dictionary/cars/brands"
        val response = httpClient.get(url)
        return response.parseResponse()
    }

    override suspend fun getCarModels(brandId: Int): List<CarModel> {
        val url = "${DEFAULT_BASE_URL}Dictionary/cars/models-of-brand"
        val response =
            httpClient.get(url) {
                parameter("brandId", brandId)
            }
        return response.parseResponse()
    }

    override suspend fun searchCities(query: String): List<City> {
        val url = "${DEFAULT_BASE_URL}Dictionary/cities/search"
        val response =
            httpClient.get(url) {
                parameter("query", query)
            }
        return response.parseResponse()
    }

    override suspend fun getCarEnums(): CarEnumsResponse {
        val url = "${DEFAULT_BASE_URL}Dictionary/enums/car"
        val response = httpClient.get(url)
        return response.parseResponse()
    }
}
