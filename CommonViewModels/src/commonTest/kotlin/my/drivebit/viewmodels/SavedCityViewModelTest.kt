package my.drivebit.viewmodels

import my.drivebit.network.services.City
import my.drivebit.repositories.MyCityRepository
import my.drivebit.repositories.SelectedCityRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class MockSelectedCityRepository : SelectedCityRepository {
    var savedCityId: Int? = null
    var savedCityName: String? = null

    override fun saveCity(
        cityId: Int,
        cityName: String,
    ) {
        savedCityId = cityId
        savedCityName = cityName
    }

    override fun getCityId(): Int? = savedCityId

    override fun getCityName(): String? = savedCityName

    override fun clearCity() {
        savedCityId = null
        savedCityName = null
    }
}

class MockMyCityRepositoryForSavedCity : MyCityRepository {
    var savedCityId: Int? = null
    var savedCityName: String? = null

    override suspend fun searchCities(query: String): List<City> = throw NotImplementedError()

    override suspend fun getSelectedCity(): City = throw NotImplementedError()

    override fun selectCity(
        cityId: Int,
        cityName: String,
    ) {
        savedCityId = cityId
        savedCityName = cityName
    }
}

class SavedCityViewModelTest {
    @Test
    fun `SavedCityViewModelForCarCreation should save city to SelectedCityRepository`() {
        val mockRepository = MockSelectedCityRepository()
        val viewModel = SavedCityViewModelForCarCreation(mockRepository)
        val city = City(id = 1, name = "Москва")

        viewModel.saveCity(city)

        assertEquals(1, mockRepository.savedCityId)
        assertEquals("Москва", mockRepository.savedCityName)
    }

    @Test
    fun `SavedCityViewModelForMyCity should save city to MyCityRepository`() {
        val mockRepository = MockMyCityRepositoryForSavedCity()
        val viewModel = SavedCityViewModelForMyCity(mockRepository)
        val city = City(id = 2, name = "Санкт-Петербург")

        viewModel.saveCity(city)

        assertEquals(2, mockRepository.savedCityId)
        assertEquals("Санкт-Петербург", mockRepository.savedCityName)
    }

    @Test
    fun `SavedCityViewModelForCarCreation should handle different cities`() {
        val mockRepository = MockSelectedCityRepository()
        val viewModel = SavedCityViewModelForCarCreation(mockRepository)

        viewModel.saveCity(City(id = 1, name = "Москва"))
        assertEquals(1, mockRepository.savedCityId)
        assertEquals("Москва", mockRepository.savedCityName)

        viewModel.saveCity(City(id = 3, name = "Калининград"))
        assertEquals(3, mockRepository.savedCityId)
        assertEquals("Калининград", mockRepository.savedCityName)
    }

    @Test
    fun `SavedCityViewModelForMyCity should handle different cities`() {
        val mockRepository = MockMyCityRepositoryForSavedCity()
        val viewModel = SavedCityViewModelForMyCity(mockRepository)

        viewModel.saveCity(City(id = 1, name = "Москва"))
        assertEquals(1, mockRepository.savedCityId)
        assertEquals("Москва", mockRepository.savedCityName)

        viewModel.saveCity(City(id = 3, name = "Калининград"))
        assertEquals(3, mockRepository.savedCityId)
        assertEquals("Калининград", mockRepository.savedCityName)
    }
}
