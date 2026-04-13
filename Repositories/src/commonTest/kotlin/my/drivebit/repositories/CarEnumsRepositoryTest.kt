package my.drivebit.repositories

import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.CarBrand
import my.drivebit.network.services.CarEnumsResponse
import my.drivebit.network.services.CarModel
import my.drivebit.network.services.City
import my.drivebit.network.services.Dictionary
import my.drivebit.network.services.DocumentEnumsResponse
import my.drivebit.network.services.FilterSuggestion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import my.drivebit.network.services.EnumItem as NetworkEnumItem

class CarEnumsRepositoryTest {
    @Test
    fun `should load enums successfully`() =
        runTest {
            val enums = createValidEnums()
            val fakeDictionary = createFakeDictionary(enums)
            val repository = CarEnumsRepositoryImpl(fakeDictionary)

            val result = repository.getEnums()

            assertEquals(enums.CarColorEnum.size, result.CarColorEnum.size)
            assertEquals(enums.BodyTypeEnum.size, result.BodyTypeEnum.size)
        }

    @Test
    fun `should throw exception on network error`() =
        runTest {
            val fakeDictionary =
                object : Dictionary {
                    override suspend fun getCarEnums(): CarEnumsResponse = throw Exception("Network error")

                    override suspend fun getCarBrands(): List<CarBrand> = emptyList()

                    override suspend fun getCarModels(brandId: Int): List<CarModel> = emptyList()

                    override suspend fun getCarBrandsExisting(): List<CarBrand> = getCarBrands()

                    override suspend fun getCarModelsExisting(brandId: Int): List<CarModel> = getCarModels(brandId)

                    override suspend fun searchCities(query: String): List<City> = emptyList()

                    override suspend fun getAllCities(): List<City> = emptyList()

                    override suspend fun getDocumentEnums(): DocumentEnumsResponse = throw NotImplementedError()

                    override suspend fun getFiltersSuggested(): List<FilterSuggestion> = emptyList()
                }
            val repository = CarEnumsRepositoryImpl(fakeDictionary)

            assertFailsWith<CarEnumsLoadException> {
                repository.getEnums()
            }
        }

    @Test
    fun `should throw exception when CarColorEnum is empty`() =
        runTest {
            val enums = createValidEnums().copy(CarColorEnum = emptyList())
            val fakeDictionary = createFakeDictionary(enums)
            val repository = CarEnumsRepositoryImpl(fakeDictionary)

            assertFailsWith<CarEnumsLoadException> {
                repository.getEnums()
            }
        }

    @Test
    fun `should get color by name`() =
        runTest {
            val enums = createValidEnums()
            val fakeDictionary = createFakeDictionary(enums)
            val repository = CarEnumsRepositoryImpl(fakeDictionary)

            val result = repository.getColorByName("White")

            assertEquals("White", result.name)
            assertEquals("Белый", result.translate)
        }

    @Test
    fun `should throw exception when color not found`() =
        runTest {
            val enums = createValidEnums()
            val fakeDictionary = createFakeDictionary(enums)
            val repository = CarEnumsRepositoryImpl(fakeDictionary)

            assertFailsWith<CarEnumsLoadException> {
                repository.getColorByName("Unknown")
            }
        }

    @Test
    fun `should get all colors`() =
        runTest {
            val enums = createValidEnums()
            val fakeDictionary = createFakeDictionary(enums)
            val repository = CarEnumsRepositoryImpl(fakeDictionary)

            val result = repository.getAllColors()

            assertEquals(2, result.size)
            assertTrue(result.any { it.name == "White" })
            assertTrue(result.any { it.name == "Black" })
        }

    @Test
    fun `should get body type by name`() =
        runTest {
            val enums = createValidEnums()
            val fakeDictionary = createFakeDictionary(enums)
            val repository = CarEnumsRepositoryImpl(fakeDictionary)

            val result = repository.getBodyTypeByName("Sedan")

            assertEquals("Sedan", result.name)
            assertEquals("Седан", result.translate)
        }

    private fun createFakeDictionary(enums: CarEnumsResponse): Dictionary =
        object : Dictionary {
            override suspend fun getCarEnums(): CarEnumsResponse = enums

            override suspend fun getCarBrands(): List<CarBrand> = emptyList()

            override suspend fun getCarModels(brandId: Int): List<CarModel> = emptyList()

            override suspend fun getCarBrandsExisting(): List<CarBrand> = getCarBrands()

            override suspend fun getCarModelsExisting(brandId: Int): List<CarModel> = getCarModels(brandId)

            override suspend fun searchCities(query: String): List<City> = emptyList()

            override suspend fun getAllCities(): List<City> = emptyList()

            override suspend fun getDocumentEnums(): DocumentEnumsResponse = throw NotImplementedError()

            override suspend fun getFiltersSuggested(): List<FilterSuggestion> = emptyList()
        }

    private fun createValidEnums(): CarEnumsResponse =
        CarEnumsResponse(
            CarColorEnum =
                listOf(
                    NetworkEnumItem(0, "White", "Белый"),
                    NetworkEnumItem(1, "Black", "Чёрный"),
                ),
            BodyTypeEnum =
                listOf(
                    NetworkEnumItem(0, "Sedan", "Седан"),
                    NetworkEnumItem(1, "Hatchback", "Хэтчбек"),
                ),
            CarStatusEnum =
                listOf(
                    NetworkEnumItem(0, "Available", "Доступен"),
                ),
            EngineTypeEnum =
                listOf(
                    NetworkEnumItem(0, "Gasoline", "Бензин"),
                ),
            TransmissionTypeEnum =
                listOf(
                    NetworkEnumItem(0, "Automatic", "Автоматическая"),
                ),
            DriveTypeEnum =
                listOf(
                    NetworkEnumItem(0, "Front", "Передний"),
                ),
            SteeringWheelSideEnum =
                listOf(
                    NetworkEnumItem(0, "Left", "Слева"),
                ),
            SeatsHeatingEnum =
                listOf(
                    NetworkEnumItem(0, "None", "Отсутствует"),
                ),
            SeatsVentilationEnum =
                listOf(
                    NetworkEnumItem(0, "None", "Отсутствует"),
                ),
            SeatsMassageEnum =
                listOf(
                    NetworkEnumItem(0, "None", "Отсутствует"),
                ),
            ClimateControlEnum =
                listOf(
                    NetworkEnumItem(0, "Basic", "Базовый"),
                ),
            DriveAssistantsEnum =
                listOf(
                    NetworkEnumItem(0, "None", "Отсутствует"),
                ),
            AlarmSystemEnum =
                listOf(
                    NetworkEnumItem(0, "None", "Отсутствует"),
                ),
            MultimediaSystemEnum =
                listOf(
                    NetworkEnumItem(0, "None", "Отсутствует"),
                ),
            CarRoofTypeEnum =
                listOf(
                    NetworkEnumItem(0, "Hardtop", "Сплошная"),
                ),
            MultimediaSystemOptionsEnum =
                listOf(
                    NetworkEnumItem(0, "AUX", "AUX"),
                ),
            ParkingAssistancesEnum =
                listOf(
                    NetworkEnumItem(0, "RearParkingSensors", "Задний парктроник"),
                ),
            TrunkSizeEnum =
                listOf(
                    NetworkEnumItem(0, "Compact", "Компактный"),
                ),
        )
}
