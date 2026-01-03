package my.drivebit.viewmodels

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.CarBody
import my.drivebit.network.services.CarDetailResponse
import my.drivebit.network.services.CarPhotoItem
import my.drivebit.network.services.CarChassis
import my.drivebit.network.services.CarGeneral
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class CarEditViewModelMappingTest {
    @Test
    fun `toFormData should map CarDetailResponse with flat structure to CarEditFormData`() =
        runTest(StandardTestDispatcher()) {
            val carResponse =
                CarDetailResponse(
                    id = "ef4d16a2-aeeb-457e-abbe-b72473634690",
                    brandId = 1,
                    brandName = "Acura",
                    modelId = 2,
                    modelName = "CL",
                    bodyType = "SUV",
                    bodyTypeTranslate = "Внедорожник",
                    driveType = "Rear",
                    driveTypeTranslate = "Задний",
                    engineType = "Diesel",
                    engineTypeTranslate = "Дизель",
                    engineVolume = 3.0,
                    productionYear = 2020,
                    seatsCount = 5,
                    licensePlate = "K128CT",
                    ValidAddressString = "Test Address",
                    photos =
                        listOf(
                            CarPhotoItem(
                                id = 1,
                                url = "https://example.com/photo.jpg",
                                uploadDate = "2024-01-01",
                            ),
                        ),
                )

            val viewModel = CarEditViewModelImpl(MockCarServiceForEdit())
            val formData = viewModel.mapToFormData(carResponse)

            assertNotNull(formData)
            assertEquals("ef4d16a2-aeeb-457e-abbe-b72473634690", formData.carId)
            assertEquals("K128CT", formData.licensePlate)
            assertEquals(1, formData.brandId)
            assertEquals("Acura", formData.brandName)
            assertEquals("Acura", formData.brandSearch)
            assertEquals(2, formData.modelId)
            assertEquals("CL", formData.modelName)
            assertEquals("CL", formData.modelSearch)
            assertEquals("SUV", formData.bodyType)
            assertEquals("Внедорожник", formData.bodyTypeTranslate)
            assertEquals("Внедорожник", formData.bodyTypeSearch)
            assertEquals("Rear", formData.driveType)
            assertEquals("Задний", formData.driveTypeTranslate)
            assertEquals("Задний", formData.driveTypeSearch)
            assertEquals("Diesel", formData.engineType)
            assertEquals("Дизель", formData.engineTypeTranslate)
            assertEquals("Дизель", formData.engineTypeSearch)
            assertEquals("3.0", formData.engineVolume)
            assertEquals("2020", formData.productionYear)
            assertEquals("5", formData.seatsCount)
            assertEquals("Test Address", formData.address)
            assertEquals(1, formData.photos.size)
            assertEquals("https://example.com/photo.jpg", formData.photos[0].url)
        }

    @Test
    fun `toFormData should map CarDetailResponse with nested structure to CarEditFormData`() =
        runTest(StandardTestDispatcher()) {
            val carResponse =
                CarDetailResponse(
                    id = "ef4d16a2-aeeb-457e-abbe-b72473634690",
                    general =
                        CarGeneral(
                            brandName = "Acura",
                            modelName = "CL",
                            year = 2020,
                            licensePlate = "K128CT",
                            seats = 5,
                        ),
                    chassis =
                        CarChassis(
                            engineVolume = 3.0,
                            engineType = "Diesel",
                            engineTypeTranslate = "Дизель",
                            driveType = "Rear",
                            driveTypeTranslate = "Задний",
                        ),
                    body =
                        CarBody(
                            bodyType = "SUV",
                            bodyTypeTranslate = "Внедорожник",
                        ),
                    ValidAddressString = "Test Address",
                    photos =
                        listOf(
                            CarPhotoItem(
                                id = 1,
                                url = "https://example.com/photo.jpg",
                                uploadDate = "2024-01-01",
                            ),
                        ),
                )

            val viewModel = CarEditViewModelImpl(MockCarServiceForEdit())
            val formData = viewModel.mapToFormData(carResponse)

            assertNotNull(formData)
            assertEquals("ef4d16a2-aeeb-457e-abbe-b72473634690", formData.carId)
            assertEquals("K128CT", formData.licensePlate)
            assertEquals(null, formData.brandId)
            assertEquals("Acura", formData.brandName)
            assertEquals("Acura", formData.brandSearch)
            assertEquals(null, formData.modelId)
            assertEquals("CL", formData.modelName)
            assertEquals("CL", formData.modelSearch)
            assertEquals("SUV", formData.bodyType)
            assertEquals("Внедорожник", formData.bodyTypeTranslate)
            assertEquals("Внедорожник", formData.bodyTypeSearch)
            assertEquals("Rear", formData.driveType)
            assertEquals("Задний", formData.driveTypeTranslate)
            assertEquals("Задний", formData.driveTypeSearch)
            assertEquals("Diesel", formData.engineType)
            assertEquals("Дизель", formData.engineTypeTranslate)
            assertEquals("Дизель", formData.engineTypeSearch)
            assertEquals("3.0", formData.engineVolume)
            assertEquals("2020", formData.productionYear)
            assertEquals("5", formData.seatsCount)
            assertEquals("Test Address", formData.address)
            assertEquals(1, formData.photos.size)
            assertEquals("https://example.com/photo.jpg", formData.photos[0].url)
        }

    @Test
    fun `toFormData should map CarDetailResponse with null values to empty strings`() =
        runTest(StandardTestDispatcher()) {
            val carResponse =
                CarDetailResponse(
                    id = "test-id",
                    brandId = null,
                    brandName = null,
                    modelId = null,
                    modelName = null,
                    bodyType = null,
                    bodyTypeTranslate = null,
                    driveType = null,
                    driveTypeTranslate = null,
                    engineType = null,
                    engineTypeTranslate = null,
                    engineVolume = null,
                    productionYear = null,
                    seatsCount = null,
                    licensePlate = null,
                    ValidAddressString = null,
                    photos = emptyList(),
                )

            val viewModel = CarEditViewModelImpl(MockCarServiceForEdit())
            val formData = viewModel.mapToFormData(carResponse)

            assertNotNull(formData)
            assertEquals("test-id", formData.carId)
            assertEquals("", formData.licensePlate)
            assertEquals(null, formData.brandId)
            assertEquals("", formData.brandName)
            assertEquals("", formData.brandSearch)
            assertEquals(null, formData.modelId)
            assertEquals("", formData.modelName)
            assertEquals("", formData.modelSearch)
            assertEquals(null, formData.bodyType)
            assertEquals("", formData.bodyTypeTranslate)
            assertEquals("", formData.bodyTypeSearch)
            assertEquals(null, formData.driveType)
            assertEquals("", formData.driveTypeTranslate)
            assertEquals("", formData.driveTypeSearch)
            assertEquals(null, formData.engineType)
            assertEquals("", formData.engineTypeTranslate)
            assertEquals("", formData.engineTypeSearch)
            assertEquals("", formData.engineVolume)
            assertEquals("", formData.productionYear)
            assertEquals("", formData.seatsCount)
            assertEquals("", formData.address)
            assertEquals(0, formData.photos.size)
        }

    @Test
    fun `toFormData should handle partial data correctly`() =
        runTest(StandardTestDispatcher()) {
            val carResponse =
                CarDetailResponse(
                    id = "test-id",
                    brandId = 1,
                    brandName = "Acura",
                    modelId = 2,
                    modelName = "CL",
                    bodyType = "SUV",
                    bodyTypeTranslate = "Внедорожник",
                    driveType = "Rear",
                    driveTypeTranslate = "Задний",
                    engineType = null,
                    engineTypeTranslate = null,
                    engineVolume = null,
                    productionYear = 2020,
                    seatsCount = null,
                    licensePlate = "K128CT",
                    ValidAddressString = null,
                    photos = emptyList(),
                )

            val viewModel = CarEditViewModelImpl(MockCarServiceForEdit())
            val formData = viewModel.mapToFormData(carResponse)

            assertNotNull(formData)
            assertEquals("K128CT", formData.licensePlate)
            assertEquals(1, formData.brandId)
            assertEquals("Acura", formData.brandName)
            assertEquals(2, formData.modelId)
            assertEquals("CL", formData.modelName)
            assertEquals("SUV", formData.bodyType)
            assertEquals("Внедорожник", formData.bodyTypeTranslate)
            assertEquals("Rear", formData.driveType)
            assertEquals("Задний", formData.driveTypeTranslate)
            assertEquals(null, formData.engineType)
            assertEquals("", formData.engineTypeTranslate)
            assertEquals("", formData.engineTypeSearch)
            assertEquals("", formData.engineVolume)
            assertEquals("2020", formData.productionYear)
            assertEquals("", formData.seatsCount)
            assertEquals("", formData.address)
        }
}
