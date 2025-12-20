package my.drivebit.viewmodels

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.CarBrand
import my.drivebit.repositories.CarBrandRepository
import my.drivebit.repositories.ResultCarBrands
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class CarBrandViewModelTest {
    @Test
    fun `should have initial empty state`() =
        runTest {
            val fakeRepository =
                object : CarBrandRepository {
                    override suspend fun getBrands(): ResultCarBrands = ResultCarBrands.Success(emptyList())
                }
            val viewModel = CarBrandViewModel(fakeRepository, this)

            assertEquals(emptyList(), viewModel.brands.first())
            assertNull(viewModel.error.first())
        }

    @Test
    fun `should load brands successfully`() =
        runTest {
            val expectedBrands =
                listOf(
                    CarBrand(id = 1, name = "Toyota"),
                    CarBrand(id = 2, name = "BMW"),
                )
            val fakeRepository =
                object : CarBrandRepository {
                    override suspend fun getBrands(): ResultCarBrands = ResultCarBrands.Success(expectedBrands)
                }
            val viewModel = CarBrandViewModel(fakeRepository, this)

            viewModel.loadBrands()
            advanceUntilIdle()

            assertEquals(expectedBrands, viewModel.brands.first())
            assertNull(viewModel.error.first())
        }

    @Test
    fun `should handle error`() =
        runTest {
            val errorMessage = "Network error"
            val fakeRepository =
                object : CarBrandRepository {
                    override suspend fun getBrands(): ResultCarBrands = ResultCarBrands.Error(errorMessage)
                }
            val viewModel = CarBrandViewModel(fakeRepository, this)

            viewModel.loadBrands()
            advanceUntilIdle()

            assertEquals(emptyList(), viewModel.brands.first())
            assertEquals(errorMessage, viewModel.error.first())
        }

    @Test
    fun `should filter brands by query`() =
        runTest {
            val allBrands =
                listOf(
                    CarBrand(id = 1, name = "Toyota"),
                    CarBrand(id = 2, name = "BMW"),
                    CarBrand(id = 3, name = "Mercedes-Benz"),
                )
            val fakeRepository =
                object : CarBrandRepository {
                    override suspend fun getBrands(): ResultCarBrands = ResultCarBrands.Success(allBrands)
                }
            val viewModel = CarBrandViewModel(fakeRepository, this)

            viewModel.loadBrands()
            advanceUntilIdle()

            assertEquals(allBrands, viewModel.brands.first())

            viewModel.updateQuery("Toy")
            assertEquals(listOf(CarBrand(id = 1, name = "Toyota")), viewModel.brands.first())

            viewModel.updateQuery("BM")
            assertEquals(listOf(CarBrand(id = 2, name = "BMW")), viewModel.brands.first())

            viewModel.clearQuery()
            assertEquals(allBrands, viewModel.brands.first())
        }

    @Test
    fun `should filter brands by cyrillic name`() =
        runTest {
            val allBrands =
                listOf(
                    CarBrand(id = 1, name = "Toyota", cyrillicName = "Тойота"),
                    CarBrand(id = 2, name = "BMW"),
                )
            val fakeRepository =
                object : CarBrandRepository {
                    override suspend fun getBrands(): ResultCarBrands = ResultCarBrands.Success(allBrands)
                }
            val viewModel = CarBrandViewModel(fakeRepository, this)

            viewModel.loadBrands()
            advanceUntilIdle()

            viewModel.updateQuery("Той")
            assertEquals(listOf(CarBrand(id = 1, name = "Toyota", cyrillicName = "Тойота")), viewModel.brands.first())
        }

    @Test
    fun `should return all brands when query is blank`() =
        runTest {
            val allBrands =
                listOf(
                    CarBrand(id = 1, name = "Toyota"),
                    CarBrand(id = 2, name = "BMW"),
                )
            val fakeRepository =
                object : CarBrandRepository {
                    override suspend fun getBrands(): ResultCarBrands = ResultCarBrands.Success(allBrands)
                }
            val viewModel = CarBrandViewModel(fakeRepository, this)

            viewModel.loadBrands()
            advanceUntilIdle()

            viewModel.updateQuery("Toy")
            assertEquals(1, viewModel.brands.first().size)

            viewModel.updateQuery("")
            assertEquals(allBrands, viewModel.brands.first())

            viewModel.updateQuery("   ")
            assertEquals(allBrands, viewModel.brands.first())
        }

    @Test
    fun `should have empty query initially`() =
        runTest {
            val fakeRepository =
                object : CarBrandRepository {
                    override suspend fun getBrands(): ResultCarBrands = ResultCarBrands.Success(emptyList())
                }
            val viewModel = CarBrandViewModel(fakeRepository, this)

            assertEquals("", viewModel.query.first())
        }

    @Test
    fun `should update query`() =
        runTest {
            val fakeRepository =
                object : CarBrandRepository {
                    override suspend fun getBrands(): ResultCarBrands = ResultCarBrands.Success(emptyList())
                }
            val viewModel = CarBrandViewModel(fakeRepository, this)

            viewModel.updateQuery("test")
            assertEquals("test", viewModel.query.first())
        }

    @Test
    fun `should clear query`() =
        runTest {
            val allBrands =
                listOf(
                    CarBrand(id = 1, name = "Toyota"),
                )
            val fakeRepository =
                object : CarBrandRepository {
                    override suspend fun getBrands(): ResultCarBrands = ResultCarBrands.Success(allBrands)
                }
            val viewModel = CarBrandViewModel(fakeRepository, this)

            viewModel.loadBrands()
            advanceUntilIdle()

            viewModel.updateQuery("Toy")
            assertEquals("Toy", viewModel.query.first())

            viewModel.clearQuery()
            assertEquals("", viewModel.query.first())
            assertEquals(allBrands, viewModel.brands.first())
        }

    @Test
    fun `should filter brands case insensitively`() =
        runTest {
            val allBrands =
                listOf(
                    CarBrand(id = 1, name = "Toyota"),
                    CarBrand(id = 2, name = "BMW"),
                )
            val fakeRepository =
                object : CarBrandRepository {
                    override suspend fun getBrands(): ResultCarBrands = ResultCarBrands.Success(allBrands)
                }
            val viewModel = CarBrandViewModel(fakeRepository, this)

            viewModel.loadBrands()
            advanceUntilIdle()

            viewModel.updateQuery("toy")
            assertEquals(listOf(CarBrand(id = 1, name = "Toyota")), viewModel.brands.first())

            viewModel.updateQuery("TOY")
            assertEquals(listOf(CarBrand(id = 1, name = "Toyota")), viewModel.brands.first())

            viewModel.updateQuery("bmw")
            assertEquals(listOf(CarBrand(id = 2, name = "BMW")), viewModel.brands.first())
        }

    @Test
    fun `should return empty list when no brands match query`() =
        runTest {
            val allBrands =
                listOf(
                    CarBrand(id = 1, name = "Toyota"),
                    CarBrand(id = 2, name = "BMW"),
                )
            val fakeRepository =
                object : CarBrandRepository {
                    override suspend fun getBrands(): ResultCarBrands = ResultCarBrands.Success(allBrands)
                }
            val viewModel = CarBrandViewModel(fakeRepository, this)

            viewModel.loadBrands()
            advanceUntilIdle()

            viewModel.updateQuery("Audi")
            assertEquals(emptyList<CarBrand>(), viewModel.brands.first())
        }
}
