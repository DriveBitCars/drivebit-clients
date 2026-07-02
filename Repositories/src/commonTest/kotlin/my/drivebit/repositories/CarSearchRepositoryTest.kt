package my.drivebit.repositories

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.Car
import my.drivebit.network.services.CarAddress
import my.drivebit.network.services.CarGeneral
import my.drivebit.network.services.CarItem
import my.drivebit.network.services.CarSearchResponse
import my.drivebit.network.services.City
import my.drivebit.network.services.Dictionary
import my.drivebit.network.services.FilterSuggestion
import my.drivebit.repositories.CurrentFiltersRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CarSearchRepositoryTest {
    private class MockCarService : Car {
        var searchCityId: String? = null
        var searchDateFrom: String? = null
        var searchDateTo: String? = null
        var searchAvailableMileagePerDayKmMin: Int? = null
        var searchDailyPriceMin: Int? = null
        var searchDailyPriceMax: Int? = null
        var searchYearMin: Int? = null
        var searchYearMax: Int? = null
        var searchSeatsMin: Int? = null
        var searchSeatsMax: Int? = null
        var searchBodyTypes: List<String>? = null
        var searchEngineTypes: List<String>? = null
        var searchColors: List<String>? = null
        var searchBrandId: Int? = null
        var searchModelId: Int? = null
        var searchDriveTypes: List<String>? = null
        var searchGeoLat: Double? = null
        var searchGeoLon: Double? = null
        var searchRadiusKm: Double? = null
        var searchPage: Int = 1
        var searchPageSize: Int = 9
        var searchResult: CarSearchResponse = CarSearchResponse(emptyList())
        var shouldThrowError = false
        var errorMessage = "Network error"

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
            geoLat: Double?,
            geoLon: Double?,
            radiusKm: Double?,
            page: Int,
            pageSize: Int,
        ): CarSearchResponse {
            if (shouldThrowError) {
                throw Exception(errorMessage)
            }
            searchCityId = cityId
            searchGeoLat = geoLat
            searchGeoLon = geoLon
            searchRadiusKm = radiusKm
            searchDateFrom = dateFrom
            searchDateTo = dateTo
            searchAvailableMileagePerDayKmMin = availableMileagePerDayKmMin
            searchDailyPriceMin = dailyPriceMin
            searchDailyPriceMax = dailyPriceMax
            searchYearMin = yearMin
            searchYearMax = yearMax
            searchSeatsMin = seatsMin
            searchSeatsMax = seatsMax
            searchBodyTypes = bodyTypes
            searchEngineTypes = engineTypes
            searchColors = colors
            searchBrandId = brandId
            searchModelId = modelId
            searchDriveTypes = driveTypes
            searchPage = page
            searchPageSize = pageSize
            return searchResult
        }

        override suspend fun getMyCars(): List<CarItem> = throw NotImplementedError()

        override suspend fun getCar(id: String) = throw NotImplementedError()

        override suspend fun createCar(request: my.drivebit.network.services.CarCreateRequest) =
            throw NotImplementedError()

        override suspend fun createOrUpdateCar(
            request: my.drivebit.network.services.CarCreateRequest,
            carId: String?,
        ) = throw NotImplementedError()

        override suspend fun deleteCar(carId: String) = throw NotImplementedError()
    }

    private class MockMyCityRepository : MyCityRepository {
        private val selectedCityFlow = MutableStateFlow<City>(City(id = 158830, name = "Москва"))
        var selectedCity: City
            get() = selectedCityFlow.value
            set(value) {
                selectedCityFlow.value = value
            }

        override suspend fun searchCities(query: String): List<City> = throw NotImplementedError()

        override val getSelectedCity: Flow<City> = selectedCityFlow

        override fun selectCity(
            cityId: Int,
            cityName: String,
        ) {
            selectedCityFlow.value = City(id = cityId, name = cityName)
        }
    }

    private class MockCurrentFiltersRepository : CurrentFiltersRepository {
        private val currentTaskShortNameState = MutableStateFlow<String?>(null)
        private val startStateFlow = MutableStateFlow<String?>(null)
        private val endStateFlow = MutableStateFlow<String?>(null)
        private val dailyRateMinState = MutableStateFlow<Int?>(null)
        private val dailyRateMaxState = MutableStateFlow<Int?>(null)
        private val brandIdState = MutableStateFlow<Int?>(null)
        private val brandNameState = MutableStateFlow<String?>(null)
        private val modelIdState = MutableStateFlow<Int?>(null)
        private val modelNameState = MutableStateFlow<String?>(null)
        private val driveTypeNameState = MutableStateFlow<String?>(null)
        private val driveTypeTranslateState = MutableStateFlow<String?>(null)
        private val bodyTypeNameState = MutableStateFlow<String?>(null)
        private val bodyTypeTranslateState = MutableStateFlow<String?>(null)
        private val seatsMinState = MutableStateFlow<Int?>(null)
        private val engineTypeNameState = MutableStateFlow<String?>(null)
        private val engineTypeTranslateState = MutableStateFlow<String?>(null)
        private val colorNameState = MutableStateFlow<String?>(null)
        private val colorTranslateState = MutableStateFlow<String?>(null)
        private val yearMinState = MutableStateFlow<Int?>(null)
        private val yearMaxState = MutableStateFlow<Int?>(null)
        private val seatsMaxState = MutableStateFlow<Int?>(null)
        private val availableMileagePerDayKmMinState = MutableStateFlow<Int?>(null)
        private val nearbySearchLatState = MutableStateFlow<Double?>(null)
        private val nearbySearchLonState = MutableStateFlow<Double?>(null)
        private val nearbyRadiusKmState = MutableStateFlow(CurrentFiltersRepository.DEFAULT_NEARBY_RADIUS_KM)
        private val currentPageState = MutableStateFlow(0)

        override val currentTaskShortName = currentTaskShortNameState.asStateFlow()
        override val startState = startStateFlow.asStateFlow()
        override val endState = endStateFlow.asStateFlow()
        override val dailyRateMin = dailyRateMinState.asStateFlow()
        override val dailyRateMax = dailyRateMaxState.asStateFlow()
        override val brandId = brandIdState.asStateFlow()
        override val brandName = brandNameState.asStateFlow()
        override val modelId = modelIdState.asStateFlow()
        override val modelName = modelNameState.asStateFlow()
        override val driveTypeName = driveTypeNameState.asStateFlow()
        override val driveTypeTranslate = driveTypeTranslateState.asStateFlow()
        override val bodyTypeName = bodyTypeNameState.asStateFlow()
        override val bodyTypeTranslate = bodyTypeTranslateState.asStateFlow()
        override val seatsMin = seatsMinState.asStateFlow()
        override val engineTypeName = engineTypeNameState.asStateFlow()
        override val engineTypeTranslate = engineTypeTranslateState.asStateFlow()
        override val colorName = colorNameState.asStateFlow()
        override val colorTranslate = colorTranslateState.asStateFlow()
        override val yearMin = yearMinState.asStateFlow()
        override val yearMax = yearMaxState.asStateFlow()
        override val seatsMax = seatsMaxState.asStateFlow()
        override val availableMileagePerDayKmMin = availableMileagePerDayKmMinState.asStateFlow()
        override val nearbySearchLat = nearbySearchLatState.asStateFlow()
        override val nearbySearchLon = nearbySearchLonState.asStateFlow()
        override val nearbyRadiusKm = nearbyRadiusKmState.asStateFlow()
        override val currentPage = currentPageState.asStateFlow()

        override fun setPage(page: Int) {
            currentPageState.value = page.coerceAtLeast(0)
        }

        override fun updateNearbySearchCenter(
            lat: Double,
            lon: Double,
        ) {
            nearbySearchLatState.value = lat
            nearbySearchLonState.value = lon
        }

        override fun updateNearbyRadiusKm(km: Int) {
            nearbyRadiusKmState.value = km
        }

        override fun updateCurrentTask(shortName: String) {
            currentTaskShortNameState.value = shortName
        }

        override fun updateStartDate(date: String?) {
            startStateFlow.value = date
        }

        override fun updateEndDate(date: String?) {
            endStateFlow.value = date
        }

        override fun updateDailyRateMin(value: Int?) {
            dailyRateMinState.value = value
        }

        override fun updateDailyRateMax(value: Int?) {
            dailyRateMaxState.value = value
        }

        override fun updateBrand(
            id: Int?,
            name: String?,
        ) {
            brandIdState.value = id
            brandNameState.value = name
        }

        override fun updateModel(
            id: Int?,
            name: String?,
        ) {
            modelIdState.value = id
            modelNameState.value = name
        }

        override fun updateDriveType(
            name: String?,
            translate: String?,
        ) {
            driveTypeNameState.value = name
            driveTypeTranslateState.value = translate
        }

        override fun updateBodyType(
            name: String?,
            translate: String?,
        ) {
            bodyTypeNameState.value = name
            bodyTypeTranslateState.value = translate
        }

        override fun updateSeatsMin(value: Int?) {
            seatsMinState.value = value
        }

        override fun updateEngineType(
            name: String?,
            translate: String?,
        ) {
            engineTypeNameState.value = name
            engineTypeTranslateState.value = translate
        }

        override fun updateColor(
            name: String?,
            translate: String?,
        ) {
            colorNameState.value = name
            colorTranslateState.value = translate
        }

        override fun updateYearMin(value: Int?) {
            yearMinState.value = value
        }

        override fun updateYearMax(value: Int?) {
            yearMaxState.value = value
        }

        override fun updateSeatsMax(value: Int?) {
            seatsMaxState.value = value
        }

        override fun updateAvailableMileagePerDayKmMin(value: Int?) {
            availableMileagePerDayKmMinState.value = value
        }
    }

    private class MockDictionary : Dictionary {
        var filtersSuggested: List<FilterSuggestion> = emptyList()

        override suspend fun getCarBrands(): List<my.drivebit.network.services.CarBrand> = throw NotImplementedError()

        override suspend fun getCarModels(brandId: Int): List<my.drivebit.network.services.CarModel> =
            throw NotImplementedError()

        override suspend fun getCarBrandsExisting(): List<my.drivebit.network.services.CarBrand> =
            throw NotImplementedError()

        override suspend fun getCarModelsExisting(brandId: Int): List<my.drivebit.network.services.CarModel> =
            throw NotImplementedError()

        override suspend fun searchCities(query: String): List<City> = throw NotImplementedError()

        override suspend fun getAllCities(): List<City> = throw NotImplementedError()

        override suspend fun getCarEnums(): my.drivebit.network.services.CarEnumsResponse = throw NotImplementedError()

        override suspend fun getDocumentEnums(): my.drivebit.network.services.DocumentEnumsResponse =
            throw NotImplementedError()

        override suspend fun getFiltersSuggested(): List<FilterSuggestion> = filtersSuggested
    }

    private fun testCarItem(
        id: String,
        brandName: String,
        modelName: String,
    ): CarItem =
        CarItem(
            id = id,
            general =
                CarGeneral(
                    brandName = brandName,
                    modelName = modelName,
                    vin = "VIN123",
                    seats = 4,
                    address =
                        CarAddress(
                            geoLat = 0.0,
                            geoLon = 0.0,
                        ),
                ),
        )

    @Test
    fun `should search cars using selected city from MyCityRepository`() =
        runTest {
            val mockCarService = MockCarService()
            val mockMyCityRepository = MockMyCityRepository()
            mockMyCityRepository.selectedCity = City(id = 158830, name = "Москва")
            val expectedCars =
                listOf(
                    testCarItem(id = "1", brandName = "BMW", modelName = "X5"),
                    testCarItem(id = "2", brandName = "Audi", modelName = "A4"),
                )
            mockCarService.searchResult = CarSearchResponse(expectedCars)
            val mockCurrentFiltersRepository = MockCurrentFiltersRepository()
            val mockDictionary = MockDictionary()
            val repository =
                CarSearchRepositoryImpl(
                    carService = mockCarService,
                    myCityRepository = mockMyCityRepository,
                    currentFiltersRepository = mockCurrentFiltersRepository,
                )

            val result = repository.searchCarsByUserCity.first()

            assertEquals("158830", mockCarService.searchCityId)
            assertEquals(null, mockCarService.searchDateFrom)
            assertEquals(null, mockCarService.searchDateTo)
            assertEquals(2, result.cars.size)
            assertEquals("BMW", result.cars[0].general.brandName)
            assertEquals("Audi", result.cars[1].general.brandName)
        }

    @Test
    fun `should pass geo params when nearby filter and center is set`() =
        runTest {
            val mockCarService = MockCarService()
            val mockMyCityRepository = MockMyCityRepository()
            mockMyCityRepository.selectedCity = City(id = 158830, name = "Москва")
            mockCarService.searchResult = CarSearchResponse(emptyList())
            val mockCurrentFiltersRepository = MockCurrentFiltersRepository()
            mockCurrentFiltersRepository.updateCurrentTask("Поблизости")
            mockCurrentFiltersRepository.updateNearbySearchCenter(55.75, 37.62)
            mockCurrentFiltersRepository.updateNearbyRadiusKm(25)
            val mockDictionary = MockDictionary()
            val repository =
                CarSearchRepositoryImpl(
                    carService = mockCarService,
                    myCityRepository = mockMyCityRepository,
                    currentFiltersRepository = mockCurrentFiltersRepository,
                )

            repository.searchCarsByUserCity.first()

            assertEquals("158830", mockCarService.searchCityId)
            assertEquals(55.75, mockCarService.searchGeoLat)
            assertEquals(37.62, mockCarService.searchGeoLon)
            assertEquals(25.0, mockCarService.searchRadiusKm)
            assertEquals(100, mockCarService.searchPageSize)
        }

    @Test
    fun `should use different city when MyCityRepository returns different city`() =
        runTest {
            val mockCarService = MockCarService()
            val mockMyCityRepository = MockMyCityRepository()
            mockMyCityRepository.selectedCity = City(id = 158831, name = "Санкт-Петербург")
            val mockCurrentFiltersRepository = MockCurrentFiltersRepository()
            val mockDictionary = MockDictionary()
            val repository =
                CarSearchRepositoryImpl(
                    carService = mockCarService,
                    myCityRepository = mockMyCityRepository,
                    currentFiltersRepository = mockCurrentFiltersRepository,
                )

            repository.searchCarsByUserCity.first()

            assertEquals("158831", mockCarService.searchCityId)
        }

    @Test
    fun `should propagate error from Car service`() =
        runTest {
            val mockCarService = MockCarService()
            mockCarService.shouldThrowError = true
            mockCarService.errorMessage = "City not found"
            val mockMyCityRepository = MockMyCityRepository()
            val mockCurrentFiltersRepository = MockCurrentFiltersRepository()
            val mockDictionary = MockDictionary()
            val repository =
                CarSearchRepositoryImpl(
                    carService = mockCarService,
                    myCityRepository = mockMyCityRepository,
                    currentFiltersRepository = mockCurrentFiltersRepository,
                )

            val exception =
                runCatching {
                    repository.searchCarsByUserCity.first()
                }.exceptionOrNull()

            assertIs<Exception>(exception)
            assertEquals("City not found", exception?.message)
        }

    @Test
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun `should automatically update search results when city changes`() =
        runTest {
            val mockCarService = MockCarService()
            val mockMyCityRepository = MockMyCityRepository()
            mockMyCityRepository.selectedCity = City(id = 158830, name = "Москва")
            val moscowCars = listOf(testCarItem(id = "1", brandName = "BMW", modelName = "X5"))
            val spbCars = listOf(testCarItem(id = "2", brandName = "Audi", modelName = "A4"))
            mockCarService.searchResult = CarSearchResponse(moscowCars)
            val mockCurrentFiltersRepository = MockCurrentFiltersRepository()
            val mockDictionary = MockDictionary()
            val repository =
                CarSearchRepositoryImpl(
                    carService = mockCarService,
                    myCityRepository = mockMyCityRepository,
                    currentFiltersRepository = mockCurrentFiltersRepository,
                )

            val resultsFlow = repository.searchCarsByUserCity
            val results = mutableListOf<CarSearchResponse>()

            coroutineScope {
                val job =
                    launch {
                        resultsFlow.take(2).toList(results)
                    }

                advanceUntilIdle()
                assertEquals(1, results.size)
                assertEquals("158830", mockCarService.searchCityId)
                assertEquals(1, results[0].cars.size)
                assertEquals("BMW", results[0].cars[0].general.brandName)

                mockCarService.searchResult = CarSearchResponse(spbCars)
                mockMyCityRepository.selectCity(158831, "Санкт-Петербург")
                advanceUntilIdle()

                job.cancel()
            }

            assertEquals(2, results.size)
            assertEquals("158831", mockCarService.searchCityId)
            assertEquals(1, results[1].cars.size)
            assertEquals("Audi", results[1].cars[0].general.brandName)
        }

    @Test
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun `should automatically update search results when currentTaskShortName changes`() =
        runTest {
            val mockCarService = MockCarService()
            val mockMyCityRepository = MockMyCityRepository()
            mockMyCityRepository.selectedCity = City(id = 158830, name = "Москва")
            val mockCurrentFiltersRepository = MockCurrentFiltersRepository()

            val firstCars = listOf(testCarItem(id = "1", brandName = "BMW", modelName = "X5"))
            val secondCars = listOf(testCarItem(id = "2", brandName = "Audi", modelName = "A4"))
            mockCarService.searchResult = CarSearchResponse(firstCars)

            val repository =
                CarSearchRepositoryImpl(
                    carService = mockCarService,
                    myCityRepository = mockMyCityRepository,
                    currentFiltersRepository = mockCurrentFiltersRepository,
                )

            val resultsFlow = repository.searchCarsByUserCity
            val results = mutableListOf<CarSearchResponse>()

            coroutineScope {
                val job =
                    launch {
                        resultsFlow.take(2).toList(results)
                    }

                advanceUntilIdle()
                assertEquals(1, results.size)
                assertEquals("158830", mockCarService.searchCityId)
                assertEquals(null, mockCarService.searchDateFrom)
                assertEquals(null, mockCarService.searchDateTo)
                assertEquals(1, results[0].cars.size)
                assertEquals("BMW", results[0].cars[0].general.brandName)

                mockCarService.searchResult = CarSearchResponse(secondCars)
                mockCurrentFiltersRepository.updateCurrentTask("В Крым")
                advanceUntilIdle()

                job.cancel()
            }

            assertEquals(2, results.size)
            assertEquals("158830", mockCarService.searchCityId)
            assertEquals(1, results[1].cars.size)
            assertEquals("Audi", results[1].cars[0].general.brandName)
        }

    @Test
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun `should pass Minivan body type when currentTaskShortName is Miniven`() =
        runTest {
            val mockCarService = MockCarService()
            val mockMyCityRepository = MockMyCityRepository()
            mockMyCityRepository.selectedCity = City(id = 158830, name = "Москва")
            val mockCurrentFiltersRepository = MockCurrentFiltersRepository()

            mockCarService.searchResult = CarSearchResponse(emptyList())
            val repository =
                CarSearchRepositoryImpl(
                    carService = mockCarService,
                    myCityRepository = mockMyCityRepository,
                    currentFiltersRepository = mockCurrentFiltersRepository,
                )

            mockCurrentFiltersRepository.updateCurrentTask("Минивэн")
            repository.searchCarsByUserCity.first()

            assertEquals("158830", mockCarService.searchCityId)
            assertEquals(null, mockCarService.searchAvailableMileagePerDayKmMin)
            assertEquals(null, mockCarService.searchDailyPriceMin)
            assertEquals(null, mockCarService.searchDailyPriceMax)
            assertEquals(null, mockCarService.searchYearMin)
            assertEquals(null, mockCarService.searchYearMax)
            assertEquals(null, mockCarService.searchSeatsMin)
            assertEquals(null, mockCarService.searchSeatsMax)
            assertEquals(listOf("Minivan"), mockCarService.searchBodyTypes)
        }

    @Test
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun `should pass SUV body type when currentTaskShortName is Vnedorozhnik`() =
        runTest {
            val mockCarService = MockCarService()
            val mockMyCityRepository = MockMyCityRepository()
            mockMyCityRepository.selectedCity = City(id = 158830, name = "Москва")
            val mockCurrentFiltersRepository = MockCurrentFiltersRepository()

            mockCarService.searchResult = CarSearchResponse(emptyList())
            val repository =
                CarSearchRepositoryImpl(
                    carService = mockCarService,
                    myCityRepository = mockMyCityRepository,
                    currentFiltersRepository = mockCurrentFiltersRepository,
                )

            mockCurrentFiltersRepository.updateCurrentTask("Внедорожник")
            repository.searchCarsByUserCity.first()

            assertEquals(listOf("SUV"), mockCarService.searchBodyTypes)
        }

    @Test
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun `should pass econom dailyPriceMax when currentTaskShortName is Ekonom`() =
        runTest {
            val mockCarService = MockCarService()
            val mockMyCityRepository = MockMyCityRepository()
            mockMyCityRepository.selectedCity = City(id = 158830, name = "Москва")
            val mockCurrentFiltersRepository = MockCurrentFiltersRepository()

            mockCarService.searchResult = CarSearchResponse(emptyList())
            val repository =
                CarSearchRepositoryImpl(
                    carService = mockCarService,
                    myCityRepository = mockMyCityRepository,
                    currentFiltersRepository = mockCurrentFiltersRepository,
                )

            mockCurrentFiltersRepository.updateCurrentTask("Эконом")
            repository.searchCarsByUserCity.first()

            assertEquals(null, mockCarService.searchDailyPriceMin)
            assertEquals(2500, mockCarService.searchDailyPriceMax)
        }

    @Test
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun `should pass premium dailyPriceMin when currentTaskShortName is Premium`() =
        runTest {
            val mockCarService = MockCarService()
            val mockMyCityRepository = MockMyCityRepository()
            mockMyCityRepository.selectedCity = City(id = 158830, name = "Москва")
            val mockCurrentFiltersRepository = MockCurrentFiltersRepository()

            mockCarService.searchResult = CarSearchResponse(emptyList())
            val repository =
                CarSearchRepositoryImpl(
                    carService = mockCarService,
                    myCityRepository = mockMyCityRepository,
                    currentFiltersRepository = mockCurrentFiltersRepository,
                )

            mockCurrentFiltersRepository.updateCurrentTask("Премиум")
            repository.searchCarsByUserCity.first()

            assertEquals(7000, mockCarService.searchDailyPriceMin)
            assertEquals(null, mockCarService.searchDailyPriceMax)
        }

    @Test
    fun `should pass start date and end date to carService search when dates are set`() =
        runTest {
            val mockCarService = MockCarService()
            val mockMyCityRepository = MockMyCityRepository()
            mockMyCityRepository.selectedCity = City(id = 158830, name = "Москва")
            val mockCurrentFiltersRepository = MockCurrentFiltersRepository()
            val mockDictionary = MockDictionary()
            val repository =
                CarSearchRepositoryImpl(
                    carService = mockCarService,
                    myCityRepository = mockMyCityRepository,
                    currentFiltersRepository = mockCurrentFiltersRepository,
                )

            mockCurrentFiltersRepository.updateStartDate("2025-02-01")
            mockCurrentFiltersRepository.updateEndDate("2025-02-15")
            repository.searchCarsByUserCity.first()

            assertEquals("158830", mockCarService.searchCityId)
            assertEquals("2025-02-01", mockCarService.searchDateFrom)
            assertEquals("2025-02-15", mockCarService.searchDateTo)
        }

    @Test
    fun `should pass null dates to carService search when dates are not set`() =
        runTest {
            val mockCarService = MockCarService()
            val mockMyCityRepository = MockMyCityRepository()
            mockMyCityRepository.selectedCity = City(id = 158830, name = "Москва")
            val mockCurrentFiltersRepository = MockCurrentFiltersRepository()
            val mockDictionary = MockDictionary()
            val repository =
                CarSearchRepositoryImpl(
                    carService = mockCarService,
                    myCityRepository = mockMyCityRepository,
                    currentFiltersRepository = mockCurrentFiltersRepository,
                )

            repository.searchCarsByUserCity.first()

            assertEquals("158830", mockCarService.searchCityId)
            assertEquals(null, mockCarService.searchDateFrom)
            assertEquals(null, mockCarService.searchDateTo)
        }

    @Test
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun `should automatically update search results when start date changes`() =
        runTest {
            val mockCarService = MockCarService()
            val mockMyCityRepository = MockMyCityRepository()
            mockMyCityRepository.selectedCity = City(id = 158830, name = "Москва")
            val mockCurrentFiltersRepository = MockCurrentFiltersRepository()
            val mockDictionary = MockDictionary()
            val repository =
                CarSearchRepositoryImpl(
                    carService = mockCarService,
                    myCityRepository = mockMyCityRepository,
                    currentFiltersRepository = mockCurrentFiltersRepository,
                )

            val resultsFlow = repository.searchCarsByUserCity
            val results = mutableListOf<CarSearchResponse>()

            coroutineScope {
                val job =
                    launch {
                        resultsFlow.take(2).toList(results)
                    }

                advanceUntilIdle()
                assertEquals(1, results.size)
                assertEquals(null, mockCarService.searchDateFrom)
                assertEquals(null, mockCarService.searchDateTo)

                mockCurrentFiltersRepository.updateStartDate("2025-02-01")
                advanceUntilIdle()

                job.cancel()
            }

            assertEquals(2, results.size)
            assertEquals("2025-02-01", mockCarService.searchDateFrom)
            assertEquals(null, mockCarService.searchDateTo)
        }

    @Test
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun `should automatically update search results when end date changes`() =
        runTest {
            val mockCarService = MockCarService()
            val mockMyCityRepository = MockMyCityRepository()
            mockMyCityRepository.selectedCity = City(id = 158830, name = "Москва")
            val mockCurrentFiltersRepository = MockCurrentFiltersRepository()
            val mockDictionary = MockDictionary()
            val repository =
                CarSearchRepositoryImpl(
                    carService = mockCarService,
                    myCityRepository = mockMyCityRepository,
                    currentFiltersRepository = mockCurrentFiltersRepository,
                )

            val resultsFlow = repository.searchCarsByUserCity
            val results = mutableListOf<CarSearchResponse>()

            coroutineScope {
                val job =
                    launch {
                        resultsFlow.take(2).toList(results)
                    }

                advanceUntilIdle()
                assertEquals(1, results.size)
                assertEquals(null, mockCarService.searchDateFrom)
                assertEquals(null, mockCarService.searchDateTo)

                mockCurrentFiltersRepository.updateEndDate("2025-02-15")
                advanceUntilIdle()

                job.cancel()
            }

            assertEquals(2, results.size)
            assertEquals(null, mockCarService.searchDateFrom)
            assertEquals("2025-02-15", mockCarService.searchDateTo)
        }

    @Test
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun `should automatically update search results when both dates change`() =
        runTest {
            val mockCarService = MockCarService()
            val mockMyCityRepository = MockMyCityRepository()
            mockMyCityRepository.selectedCity = City(id = 158830, name = "Москва")
            val mockCurrentFiltersRepository = MockCurrentFiltersRepository()
            val mockDictionary = MockDictionary()
            val repository =
                CarSearchRepositoryImpl(
                    carService = mockCarService,
                    myCityRepository = mockMyCityRepository,
                    currentFiltersRepository = mockCurrentFiltersRepository,
                )

            val resultsFlow = repository.searchCarsByUserCity
            val results = mutableListOf<CarSearchResponse>()

            coroutineScope {
                val job =
                    launch {
                        resultsFlow.take(2).toList(results)
                    }

                advanceUntilIdle()
                assertEquals(1, results.size)
                assertEquals(null, mockCarService.searchDateFrom)
                assertEquals(null, mockCarService.searchDateTo)

                mockCurrentFiltersRepository.updateStartDate("2025-02-01")
                mockCurrentFiltersRepository.updateEndDate("2025-02-15")
                advanceUntilIdle()

                job.cancel()
            }

            assertEquals(2, results.size)
            assertEquals("2025-02-01", mockCarService.searchDateFrom)
            assertEquals("2025-02-15", mockCarService.searchDateTo)
        }
}
