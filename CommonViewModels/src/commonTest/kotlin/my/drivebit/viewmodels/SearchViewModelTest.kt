package my.drivebit.viewmodels

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.NetworkException
import my.drivebit.network.services.Car
import my.drivebit.network.services.CarItem
import my.drivebit.network.services.CarSearchResponse
import my.drivebit.network.services.City
import my.drivebit.network.services.Dictionary
import my.drivebit.network.services.EnumItem
import my.drivebit.network.services.FilterSuggestion
import my.drivebit.repositories.CarSearchRepository
import my.drivebit.repositories.MyCityRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MockSearchDictionary : Dictionary {
    var shouldThrowError = false
    var shouldThrowNetworkException = false
    var errorMessage = "Network error"
    var networkExceptionStatusCode: HttpStatusCode = HttpStatusCode.InternalServerError

    var filtersResponse: List<FilterSuggestion> =
        listOf(
            FilterSuggestion(
                id = "1",
                name = "🏖️ Туризм",
                type = "tourism",
                icon = "tourism.svg",
                availableMileagePerDayKmMin = 150,
                dailyPriceMax = 6000,
                engineTypes =
                    listOf(
                        EnumItem(number = 1, name = "Diesel", translate = "Дизель"),
                    ),
            ),
            FilterSuggestion(
                id = "2",
                name = "🚗 Все",
                type = "all",
                icon = "all.svg",
            ),
        )

    override suspend fun getCarBrands(): List<my.drivebit.network.services.CarBrand> =
        throw NotImplementedError("Not used in SearchViewModel")

    override suspend fun getCarModels(brandId: Int): List<my.drivebit.network.services.CarModel> =
        throw NotImplementedError("Not used in SearchViewModel")

    override suspend fun searchCities(query: String): List<my.drivebit.network.services.City> =
        throw NotImplementedError("Not used in SearchViewModel")

    override suspend fun getAllCities(): List<my.drivebit.network.services.City> =
        throw NotImplementedError("Not used in SearchViewModel")

    override suspend fun getCarEnums(): my.drivebit.network.services.CarEnumsResponse =
        throw NotImplementedError("Not used in SearchViewModel")

    override suspend fun getDocumentEnums(): my.drivebit.network.services.DocumentEnumsResponse =
        throw NotImplementedError("Not used in SearchViewModel")

    override suspend fun getFiltersSuggested(): List<FilterSuggestion> {
        if (shouldThrowNetworkException) {
            throw NetworkException(networkExceptionStatusCode, errorMessage)
        }
        if (shouldThrowError) {
            throw Exception(errorMessage)
        }
        return filtersResponse
    }
}

class MockSearchCarService : Car {
    var shouldThrowError = false
    var shouldThrowNetworkException = false
    var errorMessage = "Network error"
    var networkExceptionStatusCode: HttpStatusCode = HttpStatusCode.InternalServerError

    var searchResult: CarSearchResponse =
        CarSearchResponse(
            cars =
                listOf(
                    CarItem(
                        id = "car-1",
                        brand = "BMW",
                        model = "X5",
                        year = 2021,
                    ),
                ),
        )

    var lastSearchParams: Map<String, Any?> = emptyMap()

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
    ): CarSearchResponse {
        lastSearchParams =
            mapOf(
                "cityId" to cityId,
                "dateFrom" to dateFrom,
                "dateTo" to dateTo,
                "availableMileagePerDayKmMin" to availableMileagePerDayKmMin,
                "dailyPriceMin" to dailyPriceMin,
                "dailyPriceMax" to dailyPriceMax,
                "yearMin" to yearMin,
                "yearMax" to yearMax,
                "seatsMin" to seatsMin,
                "seatsMax" to seatsMax,
                "bodyTypes" to bodyTypes,
                "engineTypes" to engineTypes,
                "colors" to colors,
            )

        if (shouldThrowNetworkException) {
            throw NetworkException(networkExceptionStatusCode, errorMessage)
        }
        if (shouldThrowError) {
            throw Exception(errorMessage)
        }
        return searchResult
    }

    override suspend fun getMyCars(): List<CarItem> = throw NotImplementedError("Not used in SearchViewModel")

    override suspend fun getCar(id: String): my.drivebit.network.services.CarDetailResponse =
        throw NotImplementedError("Not used in SearchViewModel")

    override suspend fun createCar(
        request: my.drivebit.network.services.CarCreateRequest,
    ): my.drivebit.network.services.CarResponse = throw NotImplementedError("Not used in SearchViewModel")

    override suspend fun createOrUpdateCar(
        request: my.drivebit.network.services.CarCreateRequest,
        carId: String?,
    ): my.drivebit.network.services.CarResponse = throw NotImplementedError("Not used in SearchViewModel")

    override suspend fun deleteCar(carId: String): Unit = throw NotImplementedError("Not used in SearchViewModel")
}

class MockSearchMyCityRepository : MyCityRepository {
    var selectedCity: City =
        City(
            id = 1,
            name = "Москва",
        )

    override val getSelectedCity: kotlinx.coroutines.flow.Flow<City>
        get() = kotlinx.coroutines.flow.flowOf(selectedCity)

    override suspend fun searchCities(query: String): List<City> = emptyList()

    override fun selectCity(
        cityId: Int,
        cityName: String,
    ) {
        selectedCity = City(id = cityId, name = cityName)
    }
}

class MockSearchCarSearchRepository : CarSearchRepository {
    var shouldThrowError = false
    var errorMessage = "Network error"
    var searchResult: CarSearchResponse =
        CarSearchResponse(
            cars =
                listOf(
                    CarItem(
                        id = "car-1",
                        brand = "BMW",
                        model = "X5",
                        year = 2021,
                    ),
                ),
        )

    override val searchCarsByUserCity: kotlinx.coroutines.flow.Flow<CarSearchResponse>
        get() {
            if (shouldThrowError) {
                return kotlinx.coroutines.flow.flow {
                    throw Exception(errorMessage)
                }
            }
            return kotlinx.coroutines.flow.flowOf(searchResult)
        }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    @Test
    fun `initial state should be Loading and filters should be loaded automatically then perform initial search`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockDictionary = MockSearchDictionary()
            val mockCarService = MockSearchCarService()
            val mockCityRepository = MockSearchMyCityRepository()
            val mockCarSearchRepository = MockSearchCarSearchRepository()
            val viewModel =
                SearchViewModelImpl(
                    dictionary = mockDictionary,
                    carService = mockCarService,
                    myCityRepository = mockCityRepository,
                    carSearchRepository = mockCarSearchRepository,
                    coroutineScope = testScope,
                )

            assertIs<SearchState.Loading>(viewModel.state.value)
            advanceUntilIdle()
            assertIs<SearchState.SearchResults>(viewModel.state.value)
            val resultsState = viewModel.state.value as SearchState.SearchResults
            assertEquals(2, resultsState.suggestedFilters.size)
            assertEquals("🏖️ Туризм", resultsState.suggestedFilters[0].title)
            assertEquals("🚗 Все", resultsState.suggestedFilters[1].title)
            assertEquals(1, resultsState.cars.size)
            assertEquals("BMW", resultsState.cars[0].brand)
        }

    @Test
    fun `loadSuggestedFilters should load filters and perform initial search`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockDictionary = MockSearchDictionary()
            val mockCarService = MockSearchCarService()
            val mockCityRepository = MockSearchMyCityRepository()
            val mockCarSearchRepository = MockSearchCarSearchRepository()
            val viewModel =
                SearchViewModelImpl(
                    dictionary = mockDictionary,
                    carService = mockCarService,
                    myCityRepository = mockCityRepository,
                    carSearchRepository = mockCarSearchRepository,
                    coroutineScope = testScope,
                )

            advanceUntilIdle()

            assertIs<SearchState.SearchResults>(viewModel.state.value)
            val resultsState = viewModel.state.value as SearchState.SearchResults
            assertEquals(2, resultsState.suggestedFilters.size)
            assertEquals("🏖️ Туризм", resultsState.suggestedFilters[0].title)
            assertEquals("1", resultsState.suggestedFilters[0].id)
            assertEquals("tourism", resultsState.suggestedFilters[0].type)
            assertEquals("tourism.svg", resultsState.suggestedFilters[0].icon)
            assertEquals(1, resultsState.cars.size)
        }

    @Test
    fun `loadSuggestedFilters should set state to Error on NetworkException`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockDictionary = MockSearchDictionary()
            mockDictionary.shouldThrowNetworkException = true
            mockDictionary.errorMessage = "Unauthorized"
            mockDictionary.networkExceptionStatusCode = HttpStatusCode.Unauthorized
            val mockCarService = MockSearchCarService()
            val mockCityRepository = MockSearchMyCityRepository()
            val mockCarSearchRepository = MockSearchCarSearchRepository()
            val viewModel =
                SearchViewModelImpl(
                    dictionary = mockDictionary,
                    carService = mockCarService,
                    myCityRepository = mockCityRepository,
                    carSearchRepository = mockCarSearchRepository,
                    coroutineScope = testScope,
                )

            viewModel.loadSuggestedFilters()
            advanceUntilIdle()

            assertIs<SearchState.Error>(viewModel.state.value)
            val errorState = viewModel.state.value as SearchState.Error
            assertEquals("Unauthorized", errorState.message)
        }

    @Test
    fun `loadSuggestedFilters should set state to Error on generic Exception`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockDictionary = MockSearchDictionary()
            mockDictionary.shouldThrowError = true
            mockDictionary.errorMessage = "Connection failed"
            val mockCarService = MockSearchCarService()
            val mockCityRepository = MockSearchMyCityRepository()
            val mockCarSearchRepository = MockSearchCarSearchRepository()
            val viewModel =
                SearchViewModelImpl(
                    dictionary = mockDictionary,
                    carService = mockCarService,
                    myCityRepository = mockCityRepository,
                    carSearchRepository = mockCarSearchRepository,
                    coroutineScope = testScope,
                )

            viewModel.loadSuggestedFilters()
            advanceUntilIdle()

            assertIs<SearchState.Error>(viewModel.state.value)
            val errorState = viewModel.state.value as SearchState.Error
            assertEquals("Connection failed", errorState.message)
        }

    @Test
    fun `loadSuggestedFilters should set state to Error with default message on Exception with empty message`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockDictionary = MockSearchDictionary()
            mockDictionary.shouldThrowError = true
            mockDictionary.errorMessage = ""
            val mockCarService = MockSearchCarService()
            val mockCityRepository = MockSearchMyCityRepository()
            val mockCarSearchRepository = MockSearchCarSearchRepository()
            val viewModel =
                SearchViewModelImpl(
                    dictionary = mockDictionary,
                    carService = mockCarService,
                    myCityRepository = mockCityRepository,
                    carSearchRepository = mockCarSearchRepository,
                    coroutineScope = testScope,
                )

            viewModel.loadSuggestedFilters()
            advanceUntilIdle()

            assertIs<SearchState.Error>(viewModel.state.value)
            val errorState = viewModel.state.value as SearchState.Error
            assertEquals("Не удалось загрузить фильтры", errorState.message)
        }

    @Test
    fun `loadSuggestedFilters should set Loading state before making request`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockDictionary = MockSearchDictionary()
            val mockCarService = MockSearchCarService()
            val mockCityRepository = MockSearchMyCityRepository()
            val mockCarSearchRepository = MockSearchCarSearchRepository()
            val viewModel =
                SearchViewModelImpl(
                    dictionary = mockDictionary,
                    carService = mockCarService,
                    myCityRepository = mockCityRepository,
                    carSearchRepository = mockCarSearchRepository,
                    coroutineScope = testScope,
                )

            viewModel.loadSuggestedFilters()

            assertIs<SearchState.Loading>(viewModel.state.value)

            advanceUntilIdle()

            assertIs<SearchState.SearchResults>(viewModel.state.value)
        }

    @Test
    fun `loadSuggestedFilters should handle empty filters list and still perform search`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockDictionary = MockSearchDictionary()
            mockDictionary.filtersResponse = emptyList()
            val mockCarService = MockSearchCarService()
            val mockCityRepository = MockSearchMyCityRepository()
            val mockCarSearchRepository = MockSearchCarSearchRepository()
            val viewModel =
                SearchViewModelImpl(
                    dictionary = mockDictionary,
                    carService = mockCarService,
                    myCityRepository = mockCityRepository,
                    carSearchRepository = mockCarSearchRepository,
                    coroutineScope = testScope,
                )

            advanceUntilIdle()

            assertIs<SearchState.SearchResults>(viewModel.state.value)
            val resultsState = viewModel.state.value as SearchState.SearchResults
            assertEquals(0, resultsState.suggestedFilters.size)
            assertEquals(1, resultsState.cars.size)
        }

    @Test
    fun `initial search should be performed automatically after loading filters`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockDictionary = MockSearchDictionary()
            val mockCarService = MockSearchCarService()
            val mockCityRepository = MockSearchMyCityRepository()
            val mockCarSearchRepository = MockSearchCarSearchRepository()
            val viewModel =
                SearchViewModelImpl(
                    dictionary = mockDictionary,
                    carService = mockCarService,
                    myCityRepository = mockCityRepository,
                    carSearchRepository = mockCarSearchRepository,
                    coroutineScope = testScope,
                )

            advanceUntilIdle()

            assertIs<SearchState.SearchResults>(viewModel.state.value)
            val resultsState = viewModel.state.value as SearchState.SearchResults
            assertEquals(1, resultsState.cars.size)
        }

    @Test
    fun `initial search should handle error and show error state`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockDictionary = MockSearchDictionary()
            val mockCarService = MockSearchCarService()
            val mockCityRepository = MockSearchMyCityRepository()
            val mockCarSearchRepository = MockSearchCarSearchRepository()
            mockCarSearchRepository.shouldThrowError = true
            mockCarSearchRepository.errorMessage = "Search failed"
            val viewModel =
                SearchViewModelImpl(
                    dictionary = mockDictionary,
                    carService = mockCarService,
                    myCityRepository = mockCityRepository,
                    carSearchRepository = mockCarSearchRepository,
                    coroutineScope = testScope,
                )

            advanceUntilIdle()

            assertIs<SearchState.Error>(viewModel.state.value)
            val errorState = viewModel.state.value as SearchState.Error
            assertEquals("Search failed", errorState.message)
        }

    @Test
    fun `searchWithFilter should set state to Searching then SearchResults`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockDictionary = MockSearchDictionary()
            val mockCarService = MockSearchCarService()
            val mockCityRepository = MockSearchMyCityRepository()
            val mockCarSearchRepository = MockSearchCarSearchRepository()
            val viewModel =
                SearchViewModelImpl(
                    dictionary = mockDictionary,
                    carService = mockCarService,
                    myCityRepository = mockCityRepository,
                    carSearchRepository = mockCarSearchRepository,
                    coroutineScope = testScope,
                )

            advanceUntilIdle()

            val filter = (viewModel.state.value as SearchState.SearchResults).suggestedFilters[0]
            viewModel.searchWithFilter(filter)
            advanceUntilIdle()

            assertIs<SearchState.SearchResults>(viewModel.state.value)
            val resultsState = viewModel.state.value as SearchState.SearchResults
            assertEquals(1, resultsState.cars.size)
            assertEquals("BMW", resultsState.cars[0].brand)
            assertEquals("X5", resultsState.cars[0].model)
        }

    @Test
    fun `searchWithFilter should pass filter parameters to carService`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockDictionary = MockSearchDictionary()
            val mockCarService = MockSearchCarService()
            val mockCityRepository = MockSearchMyCityRepository()
            val mockCarSearchRepository = MockSearchCarSearchRepository()
            val viewModel =
                SearchViewModelImpl(
                    dictionary = mockDictionary,
                    carService = mockCarService,
                    myCityRepository = mockCityRepository,
                    carSearchRepository = mockCarSearchRepository,
                    coroutineScope = testScope,
                )

            advanceUntilIdle()

            val filter = (viewModel.state.value as SearchState.SearchResults).suggestedFilters[0]
            viewModel.searchWithFilter(filter)
            advanceUntilIdle()

            assertEquals("1", mockCarService.lastSearchParams["cityId"])
            assertEquals(150, mockCarService.lastSearchParams["availableMileagePerDayKmMin"])
            assertEquals(6000, mockCarService.lastSearchParams["dailyPriceMax"])
            assertEquals(listOf("Diesel"), mockCarService.lastSearchParams["engineTypes"])
        }

    @Test
    fun `searchWithFilter should set state to Error on search failure`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockDictionary = MockSearchDictionary()
            val mockCarService = MockSearchCarService()
            mockCarService.shouldThrowError = true
            mockCarService.errorMessage = "Search failed"
            val mockCityRepository = MockSearchMyCityRepository()
            val mockCarSearchRepository = MockSearchCarSearchRepository()
            val viewModel =
                SearchViewModelImpl(
                    dictionary = mockDictionary,
                    carService = mockCarService,
                    myCityRepository = mockCityRepository,
                    carSearchRepository = mockCarSearchRepository,
                    coroutineScope = testScope,
                )

            advanceUntilIdle()

            val filter = (viewModel.state.value as SearchState.SearchResults).suggestedFilters[0]
            viewModel.searchWithFilter(filter)
            advanceUntilIdle()

            assertIs<SearchState.Error>(viewModel.state.value)
            val errorState = viewModel.state.value as SearchState.Error
            assertEquals("Search failed", errorState.message)
        }

    @Test
    fun `searchWithFilter should preserve filters in SearchResults state`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockDictionary = MockSearchDictionary()
            val mockCarService = MockSearchCarService()
            val mockCityRepository = MockSearchMyCityRepository()
            val mockCarSearchRepository = MockSearchCarSearchRepository()
            val viewModel =
                SearchViewModelImpl(
                    dictionary = mockDictionary,
                    carService = mockCarService,
                    myCityRepository = mockCityRepository,
                    carSearchRepository = mockCarSearchRepository,
                    coroutineScope = testScope,
                )

            advanceUntilIdle()

            val resultsState = viewModel.state.value as SearchState.SearchResults
            val initialFilters = resultsState.suggestedFilters

            val filter = initialFilters[0]
            viewModel.searchWithFilter(filter)
            advanceUntilIdle()

            assertIs<SearchState.SearchResults>(viewModel.state.value)
            val newResultsState = viewModel.state.value as SearchState.SearchResults
            assertEquals(initialFilters.size, newResultsState.suggestedFilters.size)
            assertEquals(initialFilters[0].title, newResultsState.suggestedFilters[0].title)
        }

    @Test
    fun `searchWithFilter should handle empty search results`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockDictionary = MockSearchDictionary()
            val mockCarService = MockSearchCarService()
            mockCarService.searchResult = CarSearchResponse(emptyList())
            val mockCityRepository = MockSearchMyCityRepository()
            val mockCarSearchRepository = MockSearchCarSearchRepository()
            mockCarSearchRepository.searchResult = CarSearchResponse(emptyList())
            val viewModel =
                SearchViewModelImpl(
                    dictionary = mockDictionary,
                    carService = mockCarService,
                    myCityRepository = mockCityRepository,
                    carSearchRepository = mockCarSearchRepository,
                    coroutineScope = testScope,
                )

            advanceUntilIdle()

            val filter = (viewModel.state.value as SearchState.SearchResults).suggestedFilters[0]
            viewModel.searchWithFilter(filter)
            advanceUntilIdle()

            assertIs<SearchState.SearchResults>(viewModel.state.value)
            val resultsState = viewModel.state.value as SearchState.SearchResults
            assertEquals(0, resultsState.cars.size)
        }
}
