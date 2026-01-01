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
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import my.drivebit.network.DEFAULT_BASE_URL
import my.drivebit.network.NetworkException
import my.drivebit.network.ValidationErrorResponse
import my.drivebit.network.defaultJson
import my.drivebit.network.handleServiceError
import my.drivebit.network.parseResponse
import my.drivebit.network.services.AddressData

interface Car {
    suspend fun createCar(request: CarCreateRequest): CarResponse

    suspend fun createOrUpdateCar(
        request: CarCreateRequest,
        carId: String? = null,
    ): CarResponse

    suspend fun getMyCars(): List<CarListItemResponse>

    suspend fun getCar(carId: String): CarDetailResponse

    suspend fun deleteCar(carId: String)
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class CarCreateRequest(
    val id: String? = null,
    val brandId: Int? = null,
    val modelId: Int? = null,
    val bodyType: String? = null,
    val driveType: String? = null,
    @EncodeDefault val engineType: String? = null,
    @EncodeDefault val engineVolume: Double? = null,
    val productionYear: Int? = null,
    val seatsCount: Int? = null,
    val licensePlate: String? = null,
    val ValidAddressString: String = "",
    val cityId: Int? = null,
    val addr: AddressData? = null,
    val hourlyRate: Double? = null,
    val dailyRate: Double? = null,
    @EncodeDefault val ParkingAssistances: List<String> = emptyList(),
    @EncodeDefault val MultimediaSystemOptions: List<String> = emptyList(),
)

@Serializable
data class UpdateCarRequest(
    @SerialName("validAddressString")
    val validAddressString: String? = null,
    val licensePlate: String? = null,
    val hourlyRate: Double? = null,
    val dailyRate: Double? = null,
    val addr: AddressData? = null,
    val description: String? = null,
    val mileage: Int? = null,
    val horsePower: Float? = null,
    val engineVolume: Float? = null,
    val color: String? = null,
    val bodyType: String? = null,
    val engineType: String? = null,
    val transmissionType: String? = null,
    val driveType: String? = null,
    val steeringWheelSide: String? = null,
    val seatsHeating: String? = null,
    val seatsVentilation: String? = null,
    val seatsMassage: String? = null,
    val climateControl: String? = null,
    val driveAssistants: String? = null,
    val acousticSystem: String? = null,
    val alarmSystem: String? = null,
    val carRoofType: String? = null,
    val multimediaSystemOptions: List<String>? = null,
    val parkingAssistances: List<String>? = null,
    // val productionYear: Int? = null,
    // val seatsCount: Int? = null,
)

@Serializable
data class CarResponse(
    val id: String,
    val brandId: Int? = null,
    val modelId: Int? = null,
    val bodyType: String? = null,
    val driveType: String? = null,
    val engineType: String? = null,
    val engineVolume: Double? = null,
    val productionYear: Int? = null,
    val seatsCount: Int? = null,
    val winCode: String? = null,
)

@Serializable
data class CarListItemResponse(
    val id: String,
    val general: GeneralProps? = null,
    val photos: List<CarPhotoItem> = emptyList(),
) {
    fun resolvedPhotos(): List<CarPhotoItem> =
        photos.ifEmpty {
            general?.photos ?: emptyList()
        }
}

@Serializable
data class CarPhotoItem(
    val id: Int,
    val url: String,
    val uploadDate: String,
)

@Serializable
data class GeneralProps(
    val brandName: String? = null,
    val modelName: String? = null,
    val year: Int? = null,
    val licensePlate: String? = null,
    val vin: String? = null,
    val seats: Int? = null,
    val mileage: Int? = null,
    val description: String? = null,
    val hourlyRate: Double? = null,
    val dailyRate: Double? = null,
    val photos: List<CarPhotoItem> = emptyList(),
)

@Serializable
data class CarDetailResponse(
    val id: String,
    val general: GeneralProps? = null,
    val chassis: ChassisProps? = null,
    val body: BodyProps? = null,
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
    val winCode: String? = null,
    val licensePlate: String? = null,
    val ValidAddressString: String? = null,
    val hourlyRate: Double? = null,
    val dailyRate: Double? = null,
    val ParkingAssistances: List<String> = emptyList(),
    val MultimediaSystemOptions: List<String> = emptyList(),
    val photos: List<CarPhotoItem> = emptyList(),
) {
    fun resolvedBrandId(): Int? = brandId

    fun resolvedBrandName(): String? = brandName ?: general?.brandName

    fun resolvedModelId(): Int? = modelId

    fun resolvedModelName(): String? = modelName ?: general?.modelName

    fun resolvedBodyType(): String? = bodyType ?: body?.bodyType

    fun resolvedBodyTypeTranslate(): String? = bodyTypeTranslate ?: body?.bodyTypeTranslate

    fun resolvedDriveType(): String? = driveType ?: chassis?.driveType

    fun resolvedDriveTypeTranslate(): String? = driveTypeTranslate ?: chassis?.driveTypeTranslate

    fun resolvedEngineType(): String? = engineType ?: chassis?.engineType

    fun resolvedEngineTypeTranslate(): String? = engineTypeTranslate ?: chassis?.engineTypeTranslate

    fun resolvedEngineVolume(): Double? = engineVolume ?: chassis?.engineVolume

    fun resolvedProductionYear(): Int? = productionYear ?: general?.year

    fun resolvedSeatsCount(): Int? = seatsCount ?: general?.seats

    fun resolvedLicensePlate(): String? = licensePlate ?: general?.licensePlate

    fun resolvedHourlyRate(): Double? = hourlyRate ?: general?.hourlyRate

    fun resolvedDailyRate(): Double? = dailyRate ?: general?.dailyRate
}

@Serializable
data class ChassisProps(
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
data class BodyProps(
    val bodyType: String? = null,
    val bodyTypeTranslate: String? = null,
    val color: String? = null,
    val colorTranslate: String? = null,
    val carRoofType: String? = null,
    val carRoofTypeTranslate: String? = null,
)

class CarImpl(
    private val httpClient: HttpClient,
) : Car {
    override suspend fun createCar(request: CarCreateRequest): CarResponse {
        val url = "${DEFAULT_BASE_URL}Car/my"
        println("🚗 [CarService] createCar called")
        println("   - URL: $url")
        println("   - Request: $request")

        return runCatching {
            val response =
                httpClient.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            println("📡 [CarService] createCar response received")
            println("   - Status: ${response.status}")
            val bodyString = response.bodyAsText()
            println("   - Body length: ${bodyString.length}")
            println("   - Body: '$bodyString'")

            if (!response.status.isSuccess()) {
                println("❌ [CarService] Error status: ${response.status}, parsing error message...")
                val errorMessage =
                    runCatching {
                        val trimmedBody = bodyString.trim()
                        if (trimmedBody.startsWith("{") && trimmedBody.endsWith("}")) {
                            val errorResponse = defaultJson.decodeFromString<ValidationErrorResponse>(trimmedBody)
                            val errorMessages =
                                errorResponse.errors?.flatMap { (_, messages) -> messages } ?: emptyList()
                            if (errorMessages.isNotEmpty()) {
                                errorMessages.joinToString(". ")
                            } else {
                                errorResponse.title ?: trimmedBody.trim('"').trim()
                            }
                        } else {
                            trimmedBody.trim('"').trim()
                        }
                    }.getOrElse {
                        bodyString.trim('"').trim()
                    }
                throw NetworkException(response.status, errorMessage)
            }

            val result: CarResponse =
                if (bodyString.isBlank()) {
                    println("⚠️ [CarService] Empty response body, but status is 200 - treating as success")
                    println("⚠️ [CarService] Note: MyCarRepository should be refreshed after car creation")
                    CarResponse(id = "")
                } else {
                    defaultJson.decodeFromString<CarResponse>(bodyString)
                }
            println("✅ [CarService] createCar successful")
            println("   - Car ID: ${result.id}")
            result
        }.handleServiceError("CarService", "createCar")
    }

    override suspend fun createOrUpdateCar(
        request: CarCreateRequest,
        carId: String?,
    ): CarResponse {
        val url = "${DEFAULT_BASE_URL}Car/my"
        println("🚗 [CarService] createOrUpdateCar called")
        println("   - URL: $url")
        println("   - CarId: $carId")

        return runCatching {
            val response =
                if (carId != null) {
                    val updateRequest =
                        UpdateCarRequest(
                            validAddressString = request.ValidAddressString.takeIf { it.isNotBlank() },
                            licensePlate = request.licensePlate?.takeIf { it.isNotBlank() },
                            bodyType = request.bodyType,
                            driveType = request.driveType,
                            engineType = request.engineType,
                            engineVolume = request.engineVolume?.toFloat(),
                            hourlyRate = request.hourlyRate,
                            dailyRate = request.dailyRate,
                            parkingAssistances = request.ParkingAssistances.takeIf { it.isNotEmpty() },
                            multimediaSystemOptions = request.MultimediaSystemOptions.takeIf { it.isNotEmpty() },
                            // productionYear = request.productionYear,
                            // seatsCount = request.seatsCount,
                        )
                    println("   - UpdateRequest (PUT): $updateRequest")
                    httpClient.put(url) {
                        contentType(ContentType.Application.Json)
                        parameter("carId", carId)
                        setBody(updateRequest)
                    }
                } else {
                    println("   - CreateRequest (POST): $request")
                    httpClient.post(url) {
                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }
                }
            println("📡 [CarService] createOrUpdateCar response received")
            println("   - Status: ${response.status}")
            val bodyString = response.bodyAsText()
            println("   - Body: $bodyString")

            if (!response.status.isSuccess()) {
                val errorMessage =
                    runCatching {
                        @kotlinx.serialization.Serializable
                        data class ErrorResponse(
                            val error: String,
                        )
                        val errorResponse = defaultJson.decodeFromString<ErrorResponse>(bodyString)
                        errorResponse.error
                    }.getOrElse {
                        bodyString.trim('"').trim()
                    }
                throw NetworkException(response.status, errorMessage)
            }

            val result: CarResponse =
                if (bodyString.isBlank()) {
                    CarResponse(id = "")
                } else {
                    defaultJson.decodeFromString(bodyString)
                }
            println("✅ [CarService] createOrUpdateCar successful")
            println("   - Car ID: ${result.id}")
            result
        }.handleServiceError("CarService", "createOrUpdateCar")
    }

    override suspend fun getMyCars(): List<CarListItemResponse> {
        val url = "${DEFAULT_BASE_URL}Car/my"
        println("🚗 [CarService] getMyCars called")
        println("   - URL: $url")

        return runCatching {
            val response = httpClient.get(url)
            println("📡 [CarService] getMyCars response received")
            println("   - Status: ${response.status}")
            val result: List<CarListItemResponse> = response.parseResponse()
            println("✅ [CarService] getMyCars successful")
            println("   - Cars count: ${result.size}")
            result
        }.handleServiceError("CarService", "getMyCars")
    }

    override suspend fun getCar(carId: String): CarDetailResponse {
        val url = "${DEFAULT_BASE_URL}Car/$carId"
        println("🚗 [CarService] getCar called")
        println("   - URL: $url")
        println("   - Car ID: $carId")

        return runCatching {
            val response = httpClient.get(url)
            println("📡 [CarService] getCar response received")
            println("   - Status: ${response.status}")
            val result: CarDetailResponse = response.parseResponse()
            println("✅ [CarService] getCar successful")
            println("   - Car ID: ${result.id}")
            result
        }.handleServiceError("CarService", "getCar")
    }

    override suspend fun deleteCar(carId: String) {
        val url = "${DEFAULT_BASE_URL}Car/my"
        println("🗑️ [CarService] deleteCar called")
        println("   - URL: $url")
        println("   - CarId: $carId")

        runCatching {
            val response =
                httpClient.delete(url) {
                    contentType(ContentType.Application.Json)
                    parameter("carId", carId)
                }
            println("📡 [CarService] deleteCar response received")
            println("   - Status: ${response.status}")

            if (!response.status.isSuccess()) {
                val bodyString = response.bodyAsText()
                val errorMessage =
                    runCatching {
                        @kotlinx.serialization.Serializable
                        data class ErrorResponse(
                            val error: String,
                        )
                        val errorResponse = defaultJson.decodeFromString<ErrorResponse>(bodyString)
                        errorResponse.error
                    }.getOrNull() ?: "Unknown error"
                throw NetworkException(response.status, errorMessage)
            }
            println("✅ [CarService] deleteCar successful")
        }.handleServiceError("CarService", "deleteCar")
    }
}
