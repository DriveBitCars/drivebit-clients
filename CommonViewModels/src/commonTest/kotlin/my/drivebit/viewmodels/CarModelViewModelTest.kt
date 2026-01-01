package my.drivebit.viewmodels

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.CarModel
import my.drivebit.repositories.CarModelRepository
import my.drivebit.repositories.ResultCarModels
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class CarModelViewModelTest {
    @Test
    fun `should have initial empty state`() =
        runTest {
            val fakeRepository =
                object : CarModelRepository {
                    override suspend fun getModels(brandId: Int): ResultCarModels = ResultCarModels.Success(emptyList())
                }
            val viewModel = CarModelViewModel(fakeRepository, this)

            assertEquals(emptyList(), viewModel.models.first())
            assertNull(viewModel.error.first())
        }

    @Test
    fun `should load models successfully`() =
        runTest {
            val expectedModels =
                listOf(
                    CarModel(id = 1, brandId = 1, name = "Corolla"),
                    CarModel(id = 2, brandId = 1, name = "Camry"),
                )
            val fakeRepository =
                object : CarModelRepository {
                    override suspend fun getModels(brandId: Int): ResultCarModels =
                        ResultCarModels.Success(expectedModels)
                }
            val viewModel = CarModelViewModel(fakeRepository, this)

            viewModel.loadModels(1)
            advanceUntilIdle()

            assertEquals(expectedModels, viewModel.models.first())
            assertNull(viewModel.error.first())
        }

    @Test
    fun `should handle error`() =
        runTest {
            val errorMessage = "Network error"
            val fakeRepository =
                object : CarModelRepository {
                    override suspend fun getModels(brandId: Int): ResultCarModels = ResultCarModels.Error(errorMessage)
                }
            val viewModel = CarModelViewModel(fakeRepository, this)

            viewModel.loadModels(1)
            advanceUntilIdle()

            assertEquals(emptyList(), viewModel.models.first())
            assertEquals(errorMessage, viewModel.error.first())
        }

    @Test
    fun `should filter models by query`() =
        runTest {
            val allModels =
                listOf(
                    CarModel(id = 1, brandId = 1, name = "Corolla"),
                    CarModel(id = 2, brandId = 1, name = "Camry"),
                    CarModel(id = 3, brandId = 1, name = "Prius"),
                )
            val fakeRepository =
                object : CarModelRepository {
                    override suspend fun getModels(brandId: Int): ResultCarModels = ResultCarModels.Success(allModels)
                }
            val viewModel = CarModelViewModel(fakeRepository, this)

            viewModel.loadModels(1)
            advanceUntilIdle()

            assertEquals(allModels, viewModel.models.first())

            viewModel.updateQuery("Cor")
            assertEquals(listOf(CarModel(id = 1, brandId = 1, name = "Corolla")), viewModel.models.first())

            viewModel.updateQuery("Cam")
            assertEquals(listOf(CarModel(id = 2, brandId = 1, name = "Camry")), viewModel.models.first())

            viewModel.clearQuery()
            assertEquals(allModels, viewModel.models.first())
        }

    @Test
    fun `should filter models by cyrillic name`() =
        runTest {
            val allModels =
                listOf(
                    CarModel(id = 1, brandId = 1, name = "Patriot", cyrillicName = "Патриот"),
                    CarModel(id = 2, brandId = 1, name = "Camry"),
                )
            val fakeRepository =
                object : CarModelRepository {
                    override suspend fun getModels(brandId: Int): ResultCarModels = ResultCarModels.Success(allModels)
                }
            val viewModel = CarModelViewModel(fakeRepository, this)

            viewModel.loadModels(1)
            advanceUntilIdle()

            viewModel.updateQuery("Пат")
            assertEquals(
                listOf(CarModel(id = 1, brandId = 1, name = "Patriot", cyrillicName = "Патриот")),
                viewModel.models.first(),
            )

            viewModel.updateQuery("Патриот")
            assertEquals(
                listOf(CarModel(id = 1, brandId = 1, name = "Patriot", cyrillicName = "Патриот")),
                viewModel.models.first(),
            )
        }

    @Test
    fun `should return all models when query is blank`() =
        runTest {
            val allModels =
                listOf(
                    CarModel(id = 1, brandId = 1, name = "Corolla"),
                    CarModel(id = 2, brandId = 1, name = "Camry"),
                )
            val fakeRepository =
                object : CarModelRepository {
                    override suspend fun getModels(brandId: Int): ResultCarModels = ResultCarModels.Success(allModels)
                }
            val viewModel = CarModelViewModel(fakeRepository, this)

            viewModel.loadModels(1)
            advanceUntilIdle()

            viewModel.updateQuery("Cor")
            assertEquals(1, viewModel.models.first().size)

            viewModel.updateQuery("")
            assertEquals(allModels, viewModel.models.first())

            viewModel.updateQuery("   ")
            assertEquals(allModels, viewModel.models.first())
        }

    @Test
    fun `should have empty query initially`() =
        runTest {
            val fakeRepository =
                object : CarModelRepository {
                    override suspend fun getModels(brandId: Int): ResultCarModels = ResultCarModels.Success(emptyList())
                }
            val viewModel = CarModelViewModel(fakeRepository, this)

            assertEquals("", viewModel.query.first())
        }

    @Test
    fun `should update query`() =
        runTest {
            val fakeRepository =
                object : CarModelRepository {
                    override suspend fun getModels(brandId: Int): ResultCarModels = ResultCarModels.Success(emptyList())
                }
            val viewModel = CarModelViewModel(fakeRepository, this)

            viewModel.updateQuery("test")
            assertEquals("test", viewModel.query.first())
        }

    @Test
    fun `should clear query`() =
        runTest {
            val allModels =
                listOf(
                    CarModel(id = 1, brandId = 1, name = "Corolla"),
                )
            val fakeRepository =
                object : CarModelRepository {
                    override suspend fun getModels(brandId: Int): ResultCarModels = ResultCarModels.Success(allModels)
                }
            val viewModel = CarModelViewModel(fakeRepository, this)

            viewModel.loadModels(1)
            advanceUntilIdle()

            viewModel.updateQuery("Cor")
            assertEquals("Cor", viewModel.query.first())

            viewModel.clearQuery()
            assertEquals("", viewModel.query.first())
            assertEquals(allModels, viewModel.models.first())
        }

    @Test
    fun `should filter models case insensitively`() =
        runTest {
            val allModels =
                listOf(
                    CarModel(id = 1, brandId = 1, name = "Corolla"),
                    CarModel(id = 2, brandId = 1, name = "Camry"),
                )
            val fakeRepository =
                object : CarModelRepository {
                    override suspend fun getModels(brandId: Int): ResultCarModels = ResultCarModels.Success(allModels)
                }
            val viewModel = CarModelViewModel(fakeRepository, this)

            viewModel.loadModels(1)
            advanceUntilIdle()

            viewModel.updateQuery("cor")
            assertEquals(listOf(CarModel(id = 1, brandId = 1, name = "Corolla")), viewModel.models.first())

            viewModel.updateQuery("COR")
            assertEquals(listOf(CarModel(id = 1, brandId = 1, name = "Corolla")), viewModel.models.first())

            viewModel.updateQuery("camry")
            assertEquals(listOf(CarModel(id = 2, brandId = 1, name = "Camry")), viewModel.models.first())
        }

    @Test
    fun `should return empty list when no models match query`() =
        runTest {
            val allModels =
                listOf(
                    CarModel(id = 1, brandId = 1, name = "Corolla"),
                    CarModel(id = 2, brandId = 1, name = "Camry"),
                )
            val fakeRepository =
                object : CarModelRepository {
                    override suspend fun getModels(brandId: Int): ResultCarModels = ResultCarModels.Success(allModels)
                }
            val viewModel = CarModelViewModel(fakeRepository, this)

            viewModel.loadModels(1)
            advanceUntilIdle()

            viewModel.updateQuery("Audi")
            assertEquals(emptyList<CarModel>(), viewModel.models.first())
        }

    @Test
    fun `should show all models initially after loading`() =
        runTest {
            val allModels =
                listOf(
                    CarModel(id = 1, brandId = 1, name = "Corolla"),
                    CarModel(id = 2, brandId = 1, name = "Camry"),
                    CarModel(id = 3, brandId = 1, name = "Prius"),
                )
            val fakeRepository =
                object : CarModelRepository {
                    override suspend fun getModels(brandId: Int): ResultCarModels = ResultCarModels.Success(allModels)
                }
            val viewModel = CarModelViewModel(fakeRepository, this)

            viewModel.loadModels(1)
            advanceUntilIdle()

            assertEquals(allModels.size, viewModel.models.first().size)
            assertEquals(allModels, viewModel.models.first())
        }
}
