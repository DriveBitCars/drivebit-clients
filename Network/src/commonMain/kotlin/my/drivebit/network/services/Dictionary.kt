package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable
import my.drivebit.network.DEFAULT_BASE_URL
import my.drivebit.network.parseResponse

interface Dictionary {
    suspend fun getCarBrands(): List<CarBrand>

    suspend fun getCarBrandsExisting(): List<CarBrand>

    suspend fun getCarModels(brandId: Int): List<CarModel>

    suspend fun getCarModelsExisting(brandId: Int): List<CarModel>

    suspend fun searchCities(query: String): List<City>

    suspend fun getAllCities(): List<City>

    suspend fun getCarEnums(): CarEnumsResponse

    suspend fun getDocumentEnums(): DocumentEnumsResponse

    suspend fun getFiltersSuggested(): List<FilterSuggestion>
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
    val regionName: String? = null,
    val municipalDistrict: String? = null,
    val cityTypeCode: String? = null,
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
    val TrunkSizeEnum: List<EnumItem>,
    val TravelDestinationEnum: List<EnumItem> = emptyList(),
    val DocumentTypeEnum: List<EnumItem>? = null,
)

@Serializable
data class EnumItem(
    val number: Int,
    val name: String,
    val translate: String,
)

@Serializable
data class DocumentEnumsResponse(
    val DocumentTypeEnum: List<EnumItem>,
    val DocumentStatusEnum: List<EnumItem>,
)

@Serializable
data class FilterSuggestion(
    val id: String? = null,
    val name: String,
    val shortName: String,
    val type: String? = null,
    val icon: String? = null,
    val iconUrl: String,
    val availableMileagePerDayKmMin: Int? = null,
    val dailyPriceMin: Int? = null,
    val dailyPriceMax: Int? = null,
    val yearMin: Int? = null,
    val yearMax: Int? = null,
    val seatsMin: Int? = null,
    val seatsMax: Int? = null,
    val bodyTypes: List<EnumItem>? = null,
    val engineTypes: List<EnumItem>? = null,
    val colors: List<EnumItem>? = null,
) {
    val title: String
        get() = name

    fun getBodyTypeNames(): List<String>? = bodyTypes?.map { it.name }

    fun getEngineTypeNames(): List<String>? = engineTypes?.map { it.name }

    fun getColorNames(): List<String>? = colors?.map { it.name }
}

class DictionaryImpl(
    private val httpClient: HttpClient,
) : Dictionary {
    override suspend fun getCarBrands(): List<CarBrand> {
        val url = "${DEFAULT_BASE_URL}Dictionary/cars/brands"
        val response = httpClient.get(url)
        return response.parseResponse()
    }

    override suspend fun getCarBrandsExisting(): List<CarBrand> {
        val url = "${DEFAULT_BASE_URL}Dictionary/cars/brands/existing"
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

    override suspend fun getCarModelsExisting(brandId: Int): List<CarModel> {
        val url = "${DEFAULT_BASE_URL}Dictionary/cars/models/existing"
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

    override suspend fun getAllCities(): List<City> {
        val url = "${DEFAULT_BASE_URL}Dictionary/cities/all"
        val response = httpClient.get(url)
        return response.parseResponse()
    }

    override suspend fun getCarEnums(): CarEnumsResponse {
        val url = "${DEFAULT_BASE_URL}Dictionary/enums/car"
        val response = httpClient.get(url)
        return response.parseResponse()
    }

    override suspend fun getDocumentEnums(): DocumentEnumsResponse {
        val url = "${DEFAULT_BASE_URL}Dictionary/enums/document"
        val response = httpClient.get(url)
        return response.parseResponse()
    }

    override suspend fun getFiltersSuggested(): List<FilterSuggestion> {
        val url = "${DEFAULT_BASE_URL}Dictionary/cars/filters/suggested"
        val response = httpClient.get(url)
        return response.parseResponse()
    }
}
