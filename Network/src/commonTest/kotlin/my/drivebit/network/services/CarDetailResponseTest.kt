package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import my.drivebit.network.parseResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CarDetailResponseTest {
    @Test
    fun `getCar should deserialize CarDetailResponse with nested structure`() =
        runTest {
            val successResponse =
                """
                {
                    "id": "ef4d16a2-aeeb-457e-abbe-b72473634690",
                    "general": {
                        "brandName": "Acura",
                        "modelName": "CL",
                        "year": 2020,
                        "licensePlate": "K128CT",
                        "vin": "",
                        "seats": 5,
                        "mileage": 0,
                        "description": null,
                        "address": {
                            "geoLat": 47.0,
                            "geoLon": 39.0
                        }
                    },
                    "chassis": {
                        "horsePower": 0,
                        "engineVolume": 3.0,
                        "hasStartStopSystem": false,
                        "engineType": "Diesel",
                        "engineTypeTranslate": "Дизель",
                        "transmissionType": "Automatic",
                        "transmissionTranslate": "Автоматическая",
                        "driveType": "Rear",
                        "driveTypeTranslate": "Задний",
                        "steeringWheelSide": "Left",
                        "steeringWheelSideTranslate": "Слева"
                    },
                    "body": {
                        "bodyType": "SUV",
                        "bodyTypeTranslate": "Внедорожник",
                        "color": "White",
                        "colorTranslate": "Белый",
                        "carRoofType": "Hardtop",
                        "carRoofTypeTranslate": "Сплошная"
                    },
                    "photos": []
                }
                """.trimIndent()

            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = successResponse,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(
                            Json {
                                ignoreUnknownKeys = true
                                isLenient = true
                                encodeDefaults = false
                            },
                        )
                    }
                }

            val response = httpClient.get("https://drivebit.ru/api/Car/test-id")
            val result: CarDetailResponse = response.parseResponse()

            assertNotNull(result)
            assertEquals("ef4d16a2-aeeb-457e-abbe-b72473634690", result.id)
            assertEquals("K128CT", result.resolvedLicensePlate())
            assertEquals("Acura", result.resolvedBrandName())
            assertEquals("CL", result.resolvedModelName())
            assertEquals("SUV", result.resolvedBodyType())
            assertEquals("Внедорожник", result.resolvedBodyTypeTranslate())
            assertEquals("Rear", result.resolvedDriveType())
            assertEquals("Задний", result.resolvedDriveTypeTranslate())
            assertEquals("Diesel", result.resolvedEngineType())
            assertEquals("Дизель", result.resolvedEngineTypeTranslate())
            assertEquals(3.0, result.resolvedEngineVolume())
            assertEquals(2020, result.resolvedProductionYear())
            assertEquals(5, result.resolvedSeatsCount())
        }

    @Test
    fun `getCar should deserialize CarDetailResponse with flat structure`() =
        runTest {
            val successResponse =
                """
                {
                    "id": "ef4d16a2-aeeb-457e-abbe-b72473634690",
                    "brandId": 1,
                    "brandName": "Acura",
                    "modelId": 2,
                    "modelName": "CL",
                    "bodyType": "SUV",
                    "bodyTypeTranslate": "Внедорожник",
                    "driveType": "Rear",
                    "driveTypeTranslate": "Задний",
                    "engineType": "Diesel",
                    "engineTypeTranslate": "Дизель",
                    "engineVolume": 3.0,
                    "productionYear": 2020,
                    "seatsCount": 5,
                    "licensePlate": "K128CT",
                    "ValidAddressString": "Test Address",
                    "insurance": "OSAGO_Included",
                    "insuranceTranslate": "ОСАГО включено",
                    "photos": []
                }
                """.trimIndent()

            val mockEngine =
                MockEngine { request ->
                    respond(
                        content = successResponse,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(
                            Json {
                                ignoreUnknownKeys = true
                                isLenient = true
                                encodeDefaults = false
                            },
                        )
                    }
                }

            val response = httpClient.get("https://drivebit.ru/api/Car/test-id")
            val result: CarDetailResponse = response.parseResponse()

            assertNotNull(result)
            assertEquals("ef4d16a2-aeeb-457e-abbe-b72473634690", result.id)
            assertEquals(1, result.brandId)
            assertEquals("Acura", result.brandName)
            assertEquals(2, result.modelId)
            assertEquals("CL", result.modelName)
            assertEquals("SUV", result.bodyType)
            assertEquals("Внедорожник", result.bodyTypeTranslate)
            assertEquals("Rear", result.driveType)
            assertEquals("Задний", result.driveTypeTranslate)
            assertEquals("Diesel", result.engineType)
            assertEquals("Дизель", result.engineTypeTranslate)
            assertEquals(3.0, result.engineVolume)
            assertEquals(2020, result.productionYear)
            assertEquals(5, result.seatsCount)
            assertEquals("K128CT", result.licensePlate)
            assertEquals("Test Address", result.ValidAddressString)
            assertEquals("OSAGO_Included", result.insurance)
            assertEquals("ОСАГО включено", result.insuranceTranslate)
            assertEquals("ОСАГО включено", result.resolvedInsuranceDisplay())
            assertEquals("Test Address", result.resolvedAddressDisplay())
        }

    @Test
    fun `resolvedAddressDisplay prefers validAddressString over structured parts`() {
        val car =
            CarDetailResponse(
                id = "x",
                general =
                    CarGeneral(
                        brandName = "A",
                        modelName = "B",
                        vin = "",
                        seats = 5,
                        address =
                            CarAddress(
                                region = "R",
                                city = "C",
                                street = "S",
                                house = "1",
                                geoLat = 0.0,
                                geoLon = 0.0,
                            ),
                    ),
                ValidAddressString = "From API",
            )
        assertEquals("From API", car.resolvedAddressDisplay())
    }

    @Test
    fun `resolvedAddressDisplay builds from general address when ValidAddressString is absent`() {
        val car =
            CarDetailResponse(
                id = "x",
                general =
                    CarGeneral(
                        brandName = "A",
                        modelName = "B",
                        vin = "",
                        seats = 5,
                        address =
                            CarAddress(
                                region = "Ростовская обл.",
                                city = "Ростов-на-Дону",
                                street = "Жлобинский",
                                house = "25",
                                geoLat = 1.0,
                                geoLon = 2.0,
                            ),
                    ),
                ValidAddressString = null,
            )
        assertEquals("Ростовская обл., Ростов-на-Дону, Жлобинский, 25", car.resolvedAddressDisplay())
    }

    @Test
    fun `resolvedAddressDisplay removes duplicate parts from structured address`() {
        val car =
            CarDetailResponse(
                id = "x",
                general =
                    CarGeneral(
                        brandName = "A",
                        modelName = "B",
                        vin = "",
                        seats = 5,
                        address =
                            CarAddress(
                                region = "Москва",
                                city = "Москва",
                                street = "Перекопская",
                                house = null,
                                geoLat = 1.0,
                                geoLon = 2.0,
                            ),
                    ),
                ValidAddressString = null,
            )
        assertEquals("Москва, Перекопская", car.resolvedAddressDisplay())
    }

    @Test
    fun `resolvedAddressDisplay falls back to structured address when validAddressString has conflicting cities`() {
        val car =
            CarDetailResponse(
                id = "x",
                general =
                    CarGeneral(
                        brandName = "A",
                        modelName = "B",
                        vin = "",
                        seats = 5,
                        address =
                            CarAddress(
                                region = "Ростовская обл.",
                                city = "Ростов-на-Дону",
                                street = "Сосновая",
                                house = "3Б",
                                geoLat = 1.0,
                                geoLon = 2.0,
                            ),
                    ),
                ValidAddressString = "Москва, Ростов-на-Дону, Сосновая, 3Б",
            )
        assertEquals("Ростовская обл., Ростов-на-Дону, Сосновая, 3Б", car.resolvedAddressDisplay())
    }

    @Test
    fun `resolvedAddressDisplay removes duplicate parts from validAddressString`() {
        val car =
            CarDetailResponse(
                id = "x",
                general =
                    CarGeneral(
                        brandName = "A",
                        modelName = "B",
                        vin = "",
                        seats = 5,
                        address = CarAddress(geoLat = 1.0, geoLon = 2.0),
                    ),
                ValidAddressString = "Москва, Москва, Перекопская",
            )
        assertEquals("Москва, Перекопская", car.resolvedAddressDisplay())
    }

    @Test
    fun `resolvedInsuranceDisplay is null when insuranceTranslate is absent`() {
        val car =
            CarDetailResponse(
                id = "x",
                general =
                    CarGeneral(
                        brandName = "A",
                        modelName = "B",
                        vin = "",
                        seats = 5,
                        address = CarAddress(geoLat = 0.0, geoLon = 0.0),
                    ),
                insurance = "OSAGO_Included",
                insuranceTranslate = null,
            )
        assertNull(car.resolvedInsuranceDisplay())
    }

    @Test
    fun `resolvedTravelDestinationsDisplay joins translates with comma`() {
        val car =
            CarDetailResponse(
                id = "x",
                general =
                    CarGeneral(
                        brandName = "A",
                        modelName = "B",
                        vin = "",
                        seats = 5,
                        address = CarAddress(geoLat = 0.0, geoLon = 0.0),
                    ),
                equipment =
                    CarEquipment(
                        allowedTravelDestinations = listOf("Belarus", "Abkhazia"),
                        allowedTravelDestinationsTranslate = listOf("Беларусь", "Абхазия"),
                    ),
            )
        assertEquals("Беларусь, Абхазия", car.resolvedTravelDestinationsDisplay())
    }

    @Test
    fun `resolvedTravelDestinationsDisplay is null when destinations are empty`() {
        val car =
            CarDetailResponse(
                id = "x",
                general =
                    CarGeneral(
                        brandName = "A",
                        modelName = "B",
                        vin = "",
                        seats = 5,
                        address = CarAddress(geoLat = 0.0, geoLon = 0.0),
                    ),
                equipment = CarEquipment(),
            )
        assertNull(car.resolvedTravelDestinationsDisplay())
    }

    @Test
    fun `getCar should deserialize equipment allowedTravelDestinations`() =
        runTest {
            val successResponse =
                """
                {
                    "id": "22e09ade-47b3-45da-9195-d0b382a88fee",
                    "general": {
                        "brandName": "BMW",
                        "modelName": "X5",
                        "year": 2020,
                        "licensePlate": "A123BC",
                        "vin": "",
                        "seats": 5,
                        "address": { "geoLat": 55.0, "geoLon": 37.0 }
                    },
                    "equipment": {
                        "allowedTravelDestinations": ["Belarus", "Crimea"],
                        "allowedTravelDestinationsTranslate": ["Беларусь", "В Крым"]
                    }
                }
                """.trimIndent()

            val mockEngine =
                MockEngine {
                    respond(
                        content = successResponse,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(
                            Json {
                                ignoreUnknownKeys = true
                                isLenient = true
                                encodeDefaults = false
                            },
                        )
                    }
                }

            val response = httpClient.get("https://drivebit.ru/api/Car/test-id")
            val result: CarDetailResponse = response.parseResponse()

            assertEquals(listOf("Belarus", "Crimea"), result.resolvedAllowedTravelDestinations())
            assertEquals(listOf("Беларусь", "В Крым"), result.resolvedAllowedTravelDestinationsTranslate())
        }

    @Test
    fun `getCar should deserialize verification badges`() =
        runTest {
            val successResponse =
                """
                {
                    "id": "22e09ade-47b3-45da-9195-d0b382a88fee",
                    "isStsVerified": true,
                    "general": {
                        "brandName": "BMW",
                        "modelName": "X5",
                        "year": 2020,
                        "licensePlate": "A123BC",
                        "vin": "",
                        "seats": 5,
                        "ownerName": "Иван",
                        "isOwnerVerified": true,
                        "address": { "geoLat": 55.0, "geoLon": 37.0 }
                    }
                }
                """.trimIndent()

            val mockEngine =
                MockEngine {
                    respond(
                        content = successResponse,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }

            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(
                            Json {
                                ignoreUnknownKeys = true
                                isLenient = true
                                encodeDefaults = false
                            },
                        )
                    }
                }

            val response = httpClient.get("https://drivebit.ru/api/Car/test-id")
            val result: CarDetailResponse = response.parseResponse()

            assertTrue(result.isStsVerified)
            assertTrue(result.general.isOwnerVerified)
        }
}
