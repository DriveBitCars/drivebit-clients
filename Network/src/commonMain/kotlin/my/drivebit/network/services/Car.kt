package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import my.drivebit.network.DEFAULT_BASE_URL
import my.drivebit.network.parseResponse

interface Car {
    suspend fun search(
        cityId: String,
        dateFrom: String? = null,
        dateTo: String? = null,
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
)

@Serializable
data class CarItem(
    val id: String,
    val brand: String? = null,
    val model: String? = null,
    val year: Int? = null,
    val price: Double? = null,
    val cityId: String? = null,
    val photos: List<String> = emptyList(),
    val general: CarGeneral? = null,
)

@Serializable
data class CarDetailResponse(
    val id: String,
    val general: CarGeneral? = null,
    val chassis: CarChassis? = null,
    val body: CarBody? = null,
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
    val seatsCount: Int? = null,
    val licensePlate: String? = null,
    val ValidAddressString: String? = null,
    val hourlyRate: Double? = null,
    val dailyRate: Double? = null,
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

    fun resolvedSeatsCount(): Int = seatsCount ?: general?.seats ?: 0

    fun resolvedBrandId(): Int? = brandId

    fun resolvedModelId(): Int? = modelId

    fun resolvedHourlyRate(): Double = hourlyRate ?: 0.0

    fun resolvedDailyRate(): Double = dailyRate ?: 0.0
}

@Serializable
data class CarGeneral(
    val brandName: String? = null,
    val modelName: String? = null,
    val year: Int? = null,
    val licensePlate: String? = null,
    val vin: String? = null,
    val seats: Int? = null,
    val mileage: Int? = null,
    val description: String? = null,
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
data class CarPhotoItem(
    val id: Int,
    val url: String,
    val uploadDate: String,
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
    val productionYear: Int? = null,
    val seatsCount: Int? = null,
    val licensePlate: String? = null,
    val ValidAddressString: String? = null,
    val cityId: String? = null,
    val addr: String? = null,
    val hourlyRate: Double? = null,
    val dailyRate: Double? = null,
    val ParkingAssistances: List<Int> = emptyList(),
    val MultimediaSystemOptions: List<Int> = emptyList(),
)

@Serializable
data class CarResponse(
    val id: String,
)

class CarImpl(
    private val httpClient: HttpClient,
) : Car {
    private fun ensureHttpsUrl(url: String): String {
        val sanitizedUrl = url.replace(" ", "%20")

        val isHttp = sanitizedUrl.startsWith("http://")
        val isHttps = sanitizedUrl.startsWith("https://")
        if (!isHttp && !isHttps) {
            return sanitizedUrl
        }

        val withoutScheme = sanitizedUrl.substringAfter("://")
        val host = withoutScheme.substringBefore("/").substringBefore(":")

        val isLocalAddress =
            host == "localhost" ||
                host == "127.0.0.1" ||
                host.startsWith("10.") ||
                host.startsWith("192.168.") ||
                (host.startsWith("172.") && host.split(".").getOrNull(1)?.toIntOrNull()?.let { it in 16..31 } == true)

        val isProductionMinIO = host == "155.212.170.94" && sanitizedUrl.contains(":9000")
        if (isProductionMinIO) {
            val path = sanitizedUrl.substringAfter(":9000")
            return if (path.startsWith("/")) path else "/$path"
        }

        if (isLocalAddress) {
            return sanitizedUrl
        }

        return if (isHttp) sanitizedUrl.replaceFirst("http://", "https://") else sanitizedUrl
    }

    private fun sanitizeCarItems(items: List<CarItem>): List<CarItem> =
        items.map { car ->
            car.copy(
                photos = car.photos.map { ensureHttpsUrl(it) },
            )
        }

    private fun sanitizeCarDetail(result: CarDetailResponse): CarDetailResponse =
        result.copy(
            photos = result.photos.map { photo ->
                photo.copy(url = ensureHttpsUrl(photo.url))
            },
        )

    override suspend fun search(
        cityId: String,
        dateFrom: String?,
        dateTo: String?,
    ): CarSearchResponse {
        val url = "${DEFAULT_BASE_URL}Car/search"
        println("📡 [CarService] search called")
        println("   - URL: $url")
        println("   - City ID: $cityId")
        println("   - Date from: ${dateFrom ?: "not specified"}")
        println("   - Date to: ${dateTo ?: "not specified"}")

        try {
            val response =
                httpClient.get(url) {
                    parameter("cityId", cityId)
                    dateFrom?.let { parameter("dateFrom", it) }
                    dateTo?.let { parameter("dateTo", it) }
                }

            println("📡 [CarService] search response received")
            val result: CarSearchResponse = response.parseResponse()
            val sanitized = result.copy(cars = sanitizeCarItems(result.cars))
            println("📡 [CarService] search successful")
            println("   - Found ${result.cars.size} cars")
            return sanitized
        } catch (e: Exception) {
            println("❌ [CarService] search failed")
            println("   - Error: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun getMyCars(): List<CarItem> {
        val url = "${DEFAULT_BASE_URL}Car/my"
        println("📡 [CarService] getMyCars called")
        println("   - URL: $url")

        try {
            val response = httpClient.get(url)
            println("📡 [CarService] getMyCars response received")
            val result: List<CarItem> = response.parseResponse()
            val sanitized = sanitizeCarItems(result)
            println("📡 [CarService] getMyCars successful")
            println("   - Found ${result.size} cars")
            return sanitized
        } catch (e: Exception) {
            println("❌ [CarService] getMyCars failed")
            println("   - Error: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun getCar(id: String): CarDetailResponse {
        val url = "${DEFAULT_BASE_URL}Car/$id"
        println("📡 [CarService] getCar called")
        println("   - URL: $url")
        println("   - Car ID: $id")

        try {
            val response = httpClient.get(url)
            println("📡 [CarService] getCar response received")
            val result: CarDetailResponse = response.parseResponse()
            val sanitized = sanitizeCarDetail(result)
            println("📡 [CarService] getCar successful")
            return sanitized
        } catch (e: Exception) {
            println("❌ [CarService] getCar failed")
            println("   - Error: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun createCar(request: CarCreateRequest): CarResponse {
        val url = "${DEFAULT_BASE_URL}Car"
        println("📡 [CarService] createCar called")
        println("   - URL: $url")

        try {
            val response =
                httpClient.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

            println("📡 [CarService] createCar response received")
            val result: CarResponse = response.parseResponse()
            println("📡 [CarService] createCar successful")
            println("   - Created car ID: ${result.id}")
            return result
        } catch (e: Exception) {
            println("❌ [CarService] createCar failed")
            println("   - Error: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun createOrUpdateCar(
        request: CarCreateRequest,
        carId: String?,
    ): CarResponse {
        val url =
            if (carId != null) {
                "${DEFAULT_BASE_URL}Car/$carId"
            } else {
                "${DEFAULT_BASE_URL}Car"
            }
        println("📡 [CarService] createOrUpdateCar called")
        println("   - URL: $url")
        println("   - Car ID: ${carId ?: "new"}")

        try {
            val response =
                if (carId != null) {
                    httpClient.put(url) {
                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }
                } else {
                    httpClient.post(url) {
                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }
                }

            println("📡 [CarService] createOrUpdateCar response received")
            val result: CarResponse = response.parseResponse()
            println("📡 [CarService] createOrUpdateCar successful")
            println("   - Car ID: ${result.id}")
            return result
        } catch (e: Exception) {
            println("❌ [CarService] createOrUpdateCar failed")
            println("   - Error: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun deleteCar(carId: String) {
        val url = "${DEFAULT_BASE_URL}Car/$carId"
        println("📡 [CarService] deleteCar called")
        println("   - URL: $url")
        println("   - Car ID: $carId")

        try {
            httpClient.delete(url)
            println("📡 [CarService] deleteCar successful")
        } catch (e: Exception) {
            println("❌ [CarService] deleteCar failed")
            println("   - Error: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}
