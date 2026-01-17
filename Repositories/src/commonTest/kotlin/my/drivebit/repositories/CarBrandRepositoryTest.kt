package my.drivebit.repositories

import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.CarBrand
import my.drivebit.network.services.CarEnumsResponse
import my.drivebit.network.services.CarModel
import my.drivebit.network.services.City
import my.drivebit.network.services.Dictionary
import my.drivebit.network.services.DocumentEnumsResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarBrandRepositoryTest {
    @Test
    fun `should return success with brands`() =
        runTest {
            val expectedBrands =
                listOf(
                    CarBrand(id = 1, name = "Toyota"),
                    CarBrand(id = 2, name = "BMW"),
                )
            val fakeDictionary =
                object : Dictionary {
                    override suspend fun getCarBrands(): List<CarBrand> = expectedBrands

                    override suspend fun getCarModels(brandId: Int): List<CarModel> = emptyList()

                    override suspend fun getCarEnums(): CarEnumsResponse = throw NotImplementedError()

                    override suspend fun searchCities(query: String): List<City> = emptyList()

                    override suspend fun getDocumentEnums(): DocumentEnumsResponse = throw NotImplementedError()
                }
            val repository = CarBrandRepositoryImpl(fakeDictionary)

            val result = repository.getBrands()

            assertTrue(result is ResultCarBrands.Success)
            assertEquals(expectedBrands, (result as ResultCarBrands.Success).brands)
        }

    @Test
    fun `should return error on exception`() =
        runTest {
            val fakeDictionary =
                object : Dictionary {
                    override suspend fun getCarBrands(): List<CarBrand> = throw Exception("Network error")

                    override suspend fun getCarModels(brandId: Int): List<CarModel> = emptyList()

                    override suspend fun getCarEnums(): CarEnumsResponse = throw NotImplementedError()

                    override suspend fun searchCities(query: String): List<City> = emptyList()

                    override suspend fun getDocumentEnums(): DocumentEnumsResponse = throw NotImplementedError()
                }
            val repository = CarBrandRepositoryImpl(fakeDictionary)

            val result = repository.getBrands()

            assertTrue(result is ResultCarBrands.Error)
            assertEquals("Network error", (result as ResultCarBrands.Error).message)
        }

    @Test
    fun `should return error with default message when exception has no message`() =
        runTest {
            val fakeDictionary =
                object : Dictionary {
                    override suspend fun getCarBrands(): List<CarBrand> = throw Exception()

                    override suspend fun getCarModels(brandId: Int): List<CarModel> = emptyList()

                    override suspend fun getCarEnums(): CarEnumsResponse = throw NotImplementedError()

                    override suspend fun searchCities(query: String): List<City> = emptyList()

                    override suspend fun getDocumentEnums(): DocumentEnumsResponse = throw NotImplementedError()
                }
            val repository = CarBrandRepositoryImpl(fakeDictionary)

            val result = repository.getBrands()

            assertTrue(result is ResultCarBrands.Error)
            assertEquals("Unknown error", (result as ResultCarBrands.Error).message)
        }
}
