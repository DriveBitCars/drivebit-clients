package my.drivebit.viewmodels

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import my.drivebit.network.services.CarListItemResponse
import my.drivebit.repositories.MyCarRepository
import my.drivebit.shared.storage.Storage
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MockMyCarRepositoryForMenu : MyCarRepository {
    private var cars: List<CarListItemResponse> = emptyList()

    fun setCars(carsList: List<CarListItemResponse>) {
        this.cars = carsList
    }

    override suspend fun getMyCar(): List<CarListItemResponse> = cars

    override fun refresh() {}
}

class MockStorageForCarMenu : Storage {
    private var isLoggedIn = false
    private var token: String? = null
    private var refreshToken: String? = null
    private val storage = mutableMapOf<String, String>()

    override fun isLogined(): Boolean = isLoggedIn

    override fun saveToken(token: String) {
        this.token = token
        this.isLoggedIn = token.isNotEmpty()
    }

    override fun getToken(): String? = token

    override fun saveRefreshToken(refreshToken: String) {
        this.refreshToken = refreshToken
    }

    override fun getRefreshToken(): String? = refreshToken

    override fun logout() {
        token = null
        refreshToken = null
        isLoggedIn = false
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

    fun setLoggedIn(loggedIn: Boolean) {
        this.isLoggedIn = loggedIn
        if (loggedIn) {
            token = "test_token"
            refreshToken = "test_refresh_token"
        } else {
            token = null
            refreshToken = null
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CarMenuViewModelTest {
    private val mockStorage = MockStorageForCarMenu()
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `should show ListYourCar when user is logged in and has no cars`() =
        runTest(testDispatcher) {
            mockStorage.setLoggedIn(true)
            val mockMyCarRepository = MockMyCarRepositoryForMenu().apply { setCars(emptyList()) }
            val viewModel = CarMenuViewModelImpl(mockStorage, mockMyCarRepository, coroutineScope = this)
            viewModel.load()
            advanceUntilIdle()

            assertEquals(CarMenuOption.ListYourCar, viewModel.menuOption.value)
        }

    @Test
    fun `should show MyCars when user is logged in and has cars`() =
        runTest(testDispatcher) {
            mockStorage.setLoggedIn(true)
            val mockMyCarRepository =
                MockMyCarRepositoryForMenu().apply {
                    setCars(
                        listOf(
                            CarListItemResponse(
                                id = "1",
                                general = null,
                                photos = emptyList(),
                            ),
                        ),
                    )
                }
            val viewModel = CarMenuViewModelImpl(mockStorage, mockMyCarRepository, coroutineScope = this)
            viewModel.load()
            advanceUntilIdle()

            assertEquals(CarMenuOption.MyCars, viewModel.menuOption.value)
        }

    @Test
    fun `should show ListYourCar when user is not logged in`() =
        runTest(testDispatcher) {
            mockStorage.setLoggedIn(false)
            val mockMyCarRepository = MockMyCarRepositoryForMenu().apply { setCars(emptyList()) }
            val viewModel = CarMenuViewModelImpl(mockStorage, mockMyCarRepository, coroutineScope = this)
            viewModel.load()
            advanceUntilIdle()

            assertEquals(CarMenuOption.ListYourCar, viewModel.menuOption.value)
        }

    @Test
    fun `should show MyCars when user has multiple cars`() =
        runTest(testDispatcher) {
            mockStorage.setLoggedIn(true)
            val mockMyCarRepository =
                MockMyCarRepositoryForMenu().apply {
                    setCars(
                        listOf(
                            CarListItemResponse(
                                id = "1",
                                general = null,
                                photos = emptyList(),
                            ),
                            CarListItemResponse(
                                id = "2",
                                general = null,
                                photos = emptyList(),
                            ),
                        ),
                    )
                }
            val viewModel = CarMenuViewModelImpl(mockStorage, mockMyCarRepository, coroutineScope = this)
            viewModel.load()
            advanceUntilIdle()

            assertEquals(CarMenuOption.MyCars, viewModel.menuOption.value)
        }

    @Test
    fun `should handle error when loading cars`() =
        runTest(testDispatcher) {
            mockStorage.setLoggedIn(true)
            val failingMyCarRepository =
                object : MyCarRepository {
                    override suspend fun getMyCar(): List<CarListItemResponse> = throw Exception("Network error")

                    override fun refresh() {}
                }

            val viewModel = CarMenuViewModelImpl(mockStorage, failingMyCarRepository, coroutineScope = this)
            viewModel.load()
            advanceUntilIdle()

            assertEquals(CarMenuOption.ListYourCar, viewModel.menuOption.value)
        }

    @Test
    fun `should update menu option when cars are added`() =
        runTest(testDispatcher) {
            mockStorage.setLoggedIn(true)
            val mockMyCarRepository = MockMyCarRepositoryForMenu().apply { setCars(emptyList()) }
            val viewModel = CarMenuViewModelImpl(mockStorage, mockMyCarRepository, coroutineScope = this)
            viewModel.load()
            advanceUntilIdle()

            assertEquals(CarMenuOption.ListYourCar, viewModel.menuOption.value)

            mockMyCarRepository.setCars(
                listOf(
                    CarListItemResponse(
                        id = "1",
                        general = null,
                        photos = emptyList(),
                    ),
                ),
            )
            viewModel.load()
            advanceUntilIdle()

            assertEquals(CarMenuOption.MyCars, viewModel.menuOption.value)
        }

    @Test
    fun `should update menu option when all cars are removed`() =
        runTest(testDispatcher) {
            mockStorage.setLoggedIn(true)
            val mockMyCarRepository =
                MockMyCarRepositoryForMenu().apply {
                    setCars(
                        listOf(
                            CarListItemResponse(
                                id = "1",
                                general = null,
                                photos = emptyList(),
                            ),
                        ),
                    )
                }
            val viewModel = CarMenuViewModelImpl(mockStorage, mockMyCarRepository, coroutineScope = this)
            viewModel.load()
            advanceUntilIdle()

            assertEquals(CarMenuOption.MyCars, viewModel.menuOption.value)

            mockMyCarRepository.setCars(emptyList())
            viewModel.load()
            advanceUntilIdle()

            assertEquals(CarMenuOption.ListYourCar, viewModel.menuOption.value)
        }
}
