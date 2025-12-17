package my.drivebit.repositories

import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.CarBrand
import my.drivebit.network.services.Dictionary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarBrandRepositoryTest {
    @Test
    fun `should return success with brands`() = runTest {
        val expectedBrands = listOf(
            CarBrand(id = 1, name = "Toyota"),
            CarBrand(id = 2, name = "BMW"),
        )
        val fakeDictionary = object : Dictionary {
            override suspend fun getCarBrands(): List<CarBrand> = expectedBrands
        }
        val repository = CarBrandRepositoryImpl(fakeDictionary)

        val result = repository.getBrands()

        assertTrue(result is ResultCarBrands.Success)
        assertEquals(expectedBrands, (result as ResultCarBrands.Success).brands)
    }

    @Test
    fun `should return error on exception`() = runTest {
        val fakeDictionary = object : Dictionary {
            override suspend fun getCarBrands(): List<CarBrand> {
                throw Exception("Network error")
            }
        }
        val repository = CarBrandRepositoryImpl(fakeDictionary)

        val result = repository.getBrands()

        assertTrue(result is ResultCarBrands.Error)
        assertEquals("Network error", (result as ResultCarBrands.Error).message)
    }

    @Test
    fun `should return error with default message when exception has no message`() = runTest {
        val fakeDictionary = object : Dictionary {
            override suspend fun getCarBrands(): List<CarBrand> {
                throw Exception()
            }
        }
        val repository = CarBrandRepositoryImpl(fakeDictionary)

        val result = repository.getBrands()

        assertTrue(result is ResultCarBrands.Error)
        assertEquals("Unknown error", (result as ResultCarBrands.Error).message)
    }
}

