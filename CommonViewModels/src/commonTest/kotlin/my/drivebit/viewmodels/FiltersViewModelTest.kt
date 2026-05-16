package my.drivebit.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import my.drivebit.network.services.Dictionary
import my.drivebit.network.services.FilterSuggestion
import my.drivebit.repositories.CurrentFiltersRepository
import my.drivebit.shared.storage.Storage
import kotlin.test.Test
import kotlin.test.assertEquals

class MockStorage : Storage {
    private val storage = mutableMapOf<String, String>()

    override fun isLogined(): Boolean = false

    override fun saveToken(token: String) {
    }

    override fun getToken(): String? = null

    override fun saveRefreshToken(refreshToken: String) {
    }

    override fun getRefreshToken(): String? = null

    override fun logout() {
        storage.clear()
    }

    override fun putString(
        key: String,
        value: String,
    ) {
        storage[key] = value
    }

    override fun getString(
        key: String,
        defaultValue: String,
    ): String = storage[key] ?: defaultValue

    override fun contains(key: String): Boolean = storage.containsKey(key)

    override fun remove(key: String) {
        storage.remove(key)
    }
}

class MockDictionary : Dictionary {
    override suspend fun getCarBrands(): List<my.drivebit.network.services.CarBrand> =
        throw NotImplementedError("Not used in FiltersViewModel")

    override suspend fun getCarModels(brandId: Int): List<my.drivebit.network.services.CarModel> =
        throw NotImplementedError("Not used in FiltersViewModel")

    override suspend fun getCarBrandsExisting(): List<my.drivebit.network.services.CarBrand> =
        throw NotImplementedError("Not used in FiltersViewModel")

    override suspend fun getCarModelsExisting(brandId: Int): List<my.drivebit.network.services.CarModel> =
        throw NotImplementedError("Not used in FiltersViewModel")

    override suspend fun searchCities(query: String): List<my.drivebit.network.services.City> =
        throw NotImplementedError("Not used in FiltersViewModel")

    override suspend fun getAllCities(): List<my.drivebit.network.services.City> =
        throw NotImplementedError("Not used in FiltersViewModel")

    override suspend fun getCarEnums(): my.drivebit.network.services.CarEnumsResponse =
        throw NotImplementedError("Not used in FiltersViewModel")

    override suspend fun getDocumentEnums(): my.drivebit.network.services.DocumentEnumsResponse =
        throw NotImplementedError("Not used in FiltersViewModel")

    override suspend fun getFiltersSuggested(): List<FilterSuggestion> = emptyList()
}

class MockCurrentFiltersRepository : CurrentFiltersRepository {
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

class FiltersViewModelTest {
    private val mockStorage = MockStorage()
    private val mockDictionary = MockDictionary()
    private val mockCurrentFiltersRepository = MockCurrentFiltersRepository()

    @Test
    fun `initial state should have correct selected filter`() =
        runTest {
            val viewModel = FiltersViewModel(mockStorage, mockDictionary, mockCurrentFiltersRepository)

            val initialState =
                withTimeout(5000) {
                    viewModel.state.first { it.filters.size >= 2 }
                }

            assertEquals("Все", initialState.selected)
            assertEquals(2, initialState.filters.size)
            assertEquals("Все", initialState.filters[0].title)
            assertEquals("Поблизости", initialState.filters[1].title)
        }

    @Test
    fun `onSelect should update selected filter when selecting All`() =
        runTest {
            val viewModel = FiltersViewModel(mockStorage, mockDictionary, mockCurrentFiltersRepository)

            withTimeout(5000) {
                viewModel.state.first { it.filters.size >= 2 }
            }

            viewModel.onSelect("Все")

            assertEquals("Все", viewModel.state.value.selected)
        }

    @Test
    fun `onSelect should update selected filter when selecting По близости`() =
        runTest {
            val viewModel = FiltersViewModel(mockStorage, mockDictionary, mockCurrentFiltersRepository)

            withTimeout(5000) {
                viewModel.state.first { it.filters.size >= 2 }
            }

            viewModel.onSelect("Поблизости")

            assertEquals("Поблизости", viewModel.state.value.selected)
        }

    @Test
    fun `onSelect should handle multiple selections correctly`() =
        runTest {
            val viewModel = FiltersViewModel(mockStorage, mockDictionary, mockCurrentFiltersRepository)

            withTimeout(5000) {
                viewModel.state.first { it.filters.size >= 2 }
            }

            viewModel.onSelect("Все")
            assertEquals("Все", viewModel.state.value.selected)

            viewModel.onSelect("Поблизости")
            assertEquals("Поблизости", viewModel.state.value.selected)

            viewModel.onSelect("Все")
            assertEquals("Все", viewModel.state.value.selected)
        }

    @Test
    fun `onSelect should maintain filters list unchanged`() =
        runTest {
            val viewModel = FiltersViewModel(mockStorage, mockDictionary, mockCurrentFiltersRepository)

            val initialFilters =
                withTimeout(5000) {
                    viewModel.state.first { it.filters.size >= 2 }.filters
                }

            viewModel.onSelect("Поблизости")

            assertEquals(initialFilters.size, viewModel.state.value.filters.size)
            assertEquals(initialFilters, viewModel.state.value.filters)
        }
}
