package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import my.drivebit.network.DEFAULT_BASE_URL
import my.drivebit.network.defaultJson
import my.drivebit.network.parseResponse
import my.drivebit.utils.extractPathFromApiUrl

internal expect fun getCurrentDomainForCar(): String

interface Car {
    suspend fun search(
        cityId: String,
        dateFrom: String? = null,
        dateTo: String? = null,
        availableMileagePerDayKmMin: Int? = null,
        dailyPriceMin: Int? = null,
        dailyPriceMax: Int? = null,
        yearMin: Int? = null,
        yearMax: Int? = null,
        seatsMin: Int? = null,
        seatsMax: Int? = null,
        bodyTypes: List<String>? = null,
        engineTypes: List<String>? = null,
        colors: List<String>? = null,
        brandId: Int? = null,
        modelId: Int? = null,
        driveTypes: List<String>? = null,
        page: Int = 1,
        pageSize: Int = 9,
    ): CarSearchResponse

    suspend fun getMyCars(): List<CarItem>

    suspend fun getCar(id: String): CarDetailResponse

    suspend fun createCar(request: CarCreateRequest): CarResponse

    suspend fun createOrUpdateCar(
        request: CarCreateRequest,
        carId: String? = null,
    ): CarResponse

    suspend fun deleteCar(carId: String)
}

@Serializable
data class CarSearchResponse(
    val cars: List<CarItem> = emptyList(),
    val totalCount: Int = 0,
    val totalPages: Int = 0,
)

@Serializable
data class CarDTOPagedResult(
    val items: List<CarItem> = emptyList(),
    val page: Int = 0,
    val pageSize: Int = 0,
    val totalCount: Int = 0,
    val totalPages: Int = 0,
)

@Serializable
data class CarItem(
    val id: String,
    val year: Int? = null,
    @JsonNames("dailyRate", "DailyRate", "Price") val price: Double? = null,
    val dailyRate4Days: Double? = null,
    val dailyRate7Days: Double? = null,
    val dailyRate14Days: Double? = null,
    val dailyRate21Days: Double? = null,
    val deposit: Double? = null,
    val cityId: String? = null,
    val photos: List<CarPhotoItem> = emptyList(),
    val general: CarGeneral,
) {
    fun minDailyPrice(): Int? {
        val all =
            listOfNotNull(
                price?.takeIf { it > 0 }?.toInt(),
                dailyRate4Days?.takeIf { it > 0 }?.toInt(),
                dailyRate7Days?.takeIf { it > 0 }?.toInt(),
                dailyRate14Days?.takeIf { it > 0 }?.toInt(),
                dailyRate21Days?.takeIf { it > 0 }?.toInt(),
            )
        return all.minOrNull()
    }
}

@Serializable
data class CarBookingItem(
    val startAt: String,
    val endAt: String,
)

@Serializable
data class CarDetailResponse(
    val id: String,
    val general: CarGeneral =
        CarGeneral(
            brandName = "",
            modelName = "",
            vin = "",
            seats = 0,
            address = CarAddress(geoLat = 0.0, geoLon = 0.0),
        ),
    val chassis: CarChassis? = null,
    val body: CarBody? = null,
    val trunk: CarTrunk? = null,
    val photos: List<CarPhotoItem> = emptyList(),
    val brandId: Int? = null,
    val brandName: String? = null,
    val modelId: Int? = null,
    val modelName: String? = null,
    val bodyType: String? = null,
    val bodyTypeTranslate: String? = null,
    val driveType: String? = null,
    val driveTypeTranslate: String? = null,
    val engineType: String? = null,
    val engineTypeTranslate: String? = null,
    val engineVolume: Double? = null,
    val productionYear: Int? = null,
    val licensePlate: String? = null,
    val ValidAddressString: String? = null,
    val hourlyRate: Double? = null,
    val dailyRate: Double? = null,
    val dailyRate4Days: Double? = null,
    val dailyRate7Days: Double? = null,
    val dailyRate14Days: Double? = null,
    val dailyRate21Days: Double? = null,
    val seatsCount: Int? = null,
    val availableMileagePerDayKm: Int? = null,
    val insurance: String? = null,
    val insuranceTranslate: String? = null,
    val deposit: Double? = null,
    val owner: String = "",
    val carBookings: List<CarBookingItem> = emptyList(),
) {
    fun resolvedLicensePlate(): String = licensePlate ?: general?.licensePlate ?: ""

    fun resolvedBrandName(): String = brandName ?: general?.brandName ?: ""

    fun resolvedModelName(): String = modelName ?: general?.modelName ?: ""

    fun resolvedBodyType(): String = bodyType ?: body?.bodyType ?: ""

    fun resolvedBodyTypeTranslate(): String = bodyTypeTranslate ?: body?.bodyTypeTranslate ?: ""

    fun resolvedDriveType(): String = driveType ?: chassis?.driveType ?: ""

    fun resolvedDriveTypeTranslate(): String = driveTypeTranslate ?: chassis?.driveTypeTranslate ?: ""

    fun resolvedEngineType(): String = engineType ?: chassis?.engineType ?: ""

    fun resolvedEngineTypeTranslate(): String = engineTypeTranslate ?: chassis?.engineTypeTranslate ?: ""

    fun resolvedEngineVolume(): Double = engineVolume ?: chassis?.engineVolume ?: 0.0

    fun resolvedProductionYear(): Int = productionYear ?: general?.year ?: 0

    fun resolvedSeatsCount(): Int = seatsCount ?: general.seats

    fun resolvedTrunkSize(): String? = trunk?.trunkSize

    fun resolvedTrunkSizeTranslate(): String? = trunk?.trunkSizeTranslate

    fun resolvedBrandId(): Int? = brandId

    fun resolvedModelId(): Int? = modelId

    fun resolvedHourlyRate(): Int = hourlyRate?.toInt() ?: 0

    fun resolvedDailyRate(): Int = dailyRate?.toInt() ?: 0

    fun resolvedDeposit(): Int = deposit?.toInt() ?: 0

    fun resolvedInsuranceDisplay(): String? = insuranceTranslate?.trim()?.takeIf { it.isNotEmpty() }
}

@Serializable
data class CarGeneral(
    val brandName: String,
    val modelName: String,
    val year: Int? = null,
    val licensePlate: String? = null,
    val vin: String,
    val seats: Int,
    val mileage: Int? = null,
    val description: String? = null,
    val ownerName: String? = null,
    val photos: List<CarPhotoItem> = emptyList(),
    val address: CarAddress,
    val owner: String? = null,
)

@Serializable
data class CarChassis(
    val horsePower: Int? = null,
    val engineVolume: Double? = null,
    val hasStartStopSystem: Boolean? = null,
    val engineType: String? = null,
    val engineTypeTranslate: String? = null,
    val transmissionType: String? = null,
    val transmissionTranslate: String? = null,
    val driveType: String? = null,
    val driveTypeTranslate: String? = null,
    val steeringWheelSide: String? = null,
    val steeringWheelSideTranslate: String? = null,
)

@Serializable
data class CarBody(
    val bodyType: String? = null,
    val bodyTypeTranslate: String? = null,
    val color: String? = null,
    val colorTranslate: String? = null,
    val carRoofType: String? = null,
    val carRoofTypeTranslate: String? = null,
)

@Serializable
data class CarTrunk(
    val trunkSize: String? = null,
    val trunkSizeTranslate: String? = null,
)

@Serializable
data class CarAddress(
    val postalCode: String? = null,
    val region: String? = null,
    val regionArea: String? = null,
    val cityType: String? = null,
    val city: String? = null,
    val street: String? = null,
    val house: String? = null,
    val geoLat: Double? = null,
    val geoLon: Double? = null,
)

object CarInsuranceType {
    const val OSAGO_INCLUDED = "OSAGO_Included"
    const val OSAGO_UNLIMITED = "OSAGO_Unlimited"
    const val KASKO_INCLUDED = "KASKO_Included"
    const val KASKO_UNLIMITED = "KASKO_Unlimited"

    val knownApiValues: Set<String> =
        setOf(OSAGO_INCLUDED, OSAGO_UNLIMITED, KASKO_INCLUDED, KASKO_UNLIMITED)

    val optionsForUi: List<Pair<String, String>> =
        listOf(
            OSAGO_INCLUDED to "ОСАГО включено",
            OSAGO_UNLIMITED to "ОСАГО без лимита",
            KASKO_INCLUDED to "КАСКО включено",
            KASKO_UNLIMITED to "КАСКО без лимита",
        )
}

@Serializable
data class CarPhotoItem(
    val id: Int,
    val url: String,
    val uploadDate: String,
    val carId: String? = null,
)

@Serializable
data class CarCreateRequest(
    val id: String = "",
    val brandId: Int? = null,
    val modelId: Int? = null,
    val bodyType: String? = null,
    val driveType: String? = null,
    val engineType: String? = null,
    val engineVolume: Double? = null,
    @SerialName("year") @JsonNames("year", "Year", "productionYear") val year: Int,
    val seats: Int? = null,
    val trunkSize: String? = null,
    val licensePlate: String? = null,
    @SerialName("validAddressString") val ValidAddressString: String? = null,
    val addr: String? = null,
    val description: String? = null,
    val hourlyRate: Int? = null,
    val dailyRate: Int? = null,
    val dailyRate4Days: Int? = null,
    val dailyRate7Days: Int? = null,
    val dailyRate14Days: Int? = null,
    val dailyRate21Days: Int? = null,
    val deposit: Int? = null,
    val availableMileagePerDayKm: Int? = null,
    val insurance: String? = null,
    @SerialName("parkingAssistances") val ParkingAssistances: List<Int> = emptyList(),
    @SerialName("multimediaSystemOptions") val MultimediaSystemOptions: List<Int> = emptyList(),
)

@Serializable
data class CarResponse(
    val id: String,
)

class CarImpl(
    private val httpClient: HttpClient,
) : Car {
    private fun sanitizeCarItems(items: List<CarItem>): List<CarItem> =
        items.map { car ->
            val photosFromGeneral = car.general.photos
            val photosFromTopLevel = car.photos
            val allPhotos = (photosFromGeneral + photosFromTopLevel).distinctBy { it.id }

            car.copy(
                photos =
                    allPhotos.map { photo ->
                        photo.copy(url = extractPathFromApiUrl(photo.url))
                    },
                general =
                    car.general.copy(
                        photos =
                            photosFromGeneral.map { photo ->
                                photo.copy(url = extractPathFromApiUrl(photo.url))
                            },
                    ),
            )
        }

    private fun sanitizeCarDetail(result: CarDetailResponse): CarDetailResponse =
        result.copy(
            photos =
                result.photos.map { photo ->
                    photo.copy(url = extractPathFromApiUrl(photo.url))
                },
            general =
                result.general.copy(
                    photos =
                        result.general.photos.map { photo ->
                            photo.copy(url = extractPathFromApiUrl(photo.url))
                        },
                ),
        )

    override suspend fun search(
        cityId: String,
        dateFrom: String?,
        dateTo: String?,
        availableMileagePerDayKmMin: Int?,
        dailyPriceMin: Int?,
        dailyPriceMax: Int?,
        yearMin: Int?,
        yearMax: Int?,
        seatsMin: Int?,
        seatsMax: Int?,
        bodyTypes: List<String>?,
        engineTypes: List<String>?,
        colors: List<String>?,
        brandId: Int?,
        modelId: Int?,
        driveTypes: List<String>?,
        page: Int,
        pageSize: Int,
    ): CarSearchResponse {
        val url = "${DEFAULT_BASE_URL}Car/list/filtered/$cityId"
        val response =
            httpClient.get(url) {
                parameter("page", page)
                parameter("pageSize", pageSize)
                dateFrom?.let { parameter("BookingStart", it) }
                dateTo?.let { parameter("BookingEnd", it) }
                availableMileagePerDayKmMin?.let { parameter("AvailableMileagePerDayKmMin", it) }
                dailyPriceMin?.let { parameter("DailyRateMin", it) }
                dailyPriceMax?.let { parameter("DailyRateMax", it) }
                yearMin?.let { parameter("YearMin", it) }
                yearMax?.let { parameter("YearMax", it) }
                seatsMin?.let { parameter("SeatsMin", it) }
                bodyTypes?.forEach { parameter("BodyType", it) }
                engineTypes?.forEach { parameter("EngineType", it) }
                colors?.forEach { parameter("Color", it) }
                brandId?.let { parameter("BrandId", it) }
                modelId?.let { parameter("ModelId", it) }
                driveTypes?.forEach { parameter("DriveType", it) }
            }
        val result: CarDTOPagedResult = response.parseResponse()
        return CarSearchResponse(
            cars = sanitizeCarItems(result.items),
            totalCount = result.totalCount,
            totalPages = result.totalPages,
        )
    }

    override suspend fun getMyCars(): List<CarItem> {
        val url = "${DEFAULT_BASE_URL}Car/my"
        val response = httpClient.get(url)
        val result: List<CarItem> = response.parseResponse()
        return sanitizeCarItems(result)
    }

    override suspend fun getCar(id: String): CarDetailResponse {
        if (id.isBlank()) {
            throw IllegalArgumentException("Car ID cannot be empty")
        }
        val url = "${DEFAULT_BASE_URL}Car/$id"
        val response = httpClient.get(url)
        val result: CarDetailResponse = response.parseResponse()
        return sanitizeCarDetail(result)
    }

    override suspend fun createCar(request: CarCreateRequest): CarResponse {
        val url = "${DEFAULT_BASE_URL}Car/my"
        val response =
            httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        if (!response.status.isSuccess()) {
            throw my.drivebit.network.NetworkException(
                response.status,
                response.bodyAsText().takeIf { it.isNotBlank() } ?: "Ошибка создания автомобиля",
            )
        }
        val bodyString = response.bodyAsText()
        return if (bodyString.isBlank()) {
            CarResponse(id = "")
        } else {
            defaultJson.decodeFromString<CarResponse>(bodyString)
        }
    }

    override suspend fun createOrUpdateCar(
        request: CarCreateRequest,
        carId: String?,
    ): CarResponse {
        val url = "${DEFAULT_BASE_URL}Car/my"
        val response =
            if (carId != null) {
                httpClient.put(url) {
                    parameter("carId", carId)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            } else {
                httpClient.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            }
        val bodyString = response.bodyAsText()
        return if (bodyString.isBlank()) {
            CarResponse(id = carId ?: request.id)
        } else {
            defaultJson.decodeFromString<CarResponse>(bodyString)
        }
    }

    override suspend fun deleteCar(carId: String) {
        val url = "${DEFAULT_BASE_URL}Car/my"
        httpClient.delete(url) {
            parameter("carId", carId)
        }
    }
}
