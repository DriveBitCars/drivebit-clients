package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.Car
import my.drivebit.network.services.CarAddress
import my.drivebit.network.services.CarCreateRequest
import my.drivebit.network.services.CarDetailResponse
import my.drivebit.network.services.CarGeneral
import my.drivebit.network.services.CarItem
import my.drivebit.network.services.CarResponse
import my.drivebit.network.services.CarSearchResponse
import my.drivebit.network.services.Document
import my.drivebit.network.services.Documents
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class CarStsDocumentsViewModelTest {
    @Test
    fun `filterStsDocumentsForCar uses STS without carId only for single car owner`() {
        val carId = "car-1"
        val docs =
            listOf(
                Document(
                    id = 104,
                    type = CarStsDocumentTypes.BACK,
                    carId = null,
                    status = "Pending",
                    uploadDate = "2026-05-25T14:45:47Z",
                ),
            )
        assertEquals(1, CarStsDocumentsViewModelImpl.filterStsDocumentsForCar(docs, carId, myCarCount = 1).size)
        assertEquals(
            0,
            CarStsDocumentsViewModelImpl.filterStsDocumentsForCar(docs, carId, myCarCount = 2).size,
        )
    }

    @Test
    fun `filterStsDocumentsForCar matches by carId when API returns CarId`() {
        val docs =
            listOf(
                Document(
                    id = 1,
                    type = CarStsDocumentTypes.FRONT,
                    carId = "car-a",
                    status = "Pending",
                ),
                Document(
                    id = 2,
                    type = CarStsDocumentTypes.FRONT,
                    carId = "car-b",
                    status = "Pending",
                ),
            )
        val filtered = CarStsDocumentsViewModelImpl.filterStsDocumentsForCar(docs, "car-b", myCarCount = 3)
        assertEquals(1, filtered.size)
        assertEquals(2, filtered.first().id)
    }

    @Test
    fun `load filters STS documents by carId`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val carId = "car-1"
            val documents =
                FakeStsDocumentsService(
                    allDocuments =
                        listOf(
                            Document(
                                id = 1,
                                type = CarStsDocumentTypes.FRONT,
                                carId = carId,
                                status = "Pending",
                            ),
                            Document(
                                id = 2,
                                type = CarStsDocumentTypes.FRONT,
                                carId = "car-2",
                                status = "Pending",
                            ),
                            Document(
                                id = 3,
                                type = CarStsDocumentTypes.BACK,
                                carId = carId,
                                status = "Expired",
                            ),
                        ),
                )
            val viewModel =
                CarStsDocumentsViewModelImpl(
                    carId = carId,
                    documents = documents,
                    car = FakeStsCarService(stsSeriesNumber = "77 УН 123456"),
                    coroutineScope = testScope,
                )

            viewModel.load()
            advanceUntilIdle()

            val success = assertIs<CarStsDocumentsState.Success>(viewModel.state.value)
            assertEquals(1, success.documents.size)
            assertEquals(1, success.documents.first().id)
            assertEquals("77 УН 123456", success.stsSeriesNumber)
            testScope.coroutineContext.cancelChildren()
        }

    @Test
    fun `uploadDocument sends carId and deletes existing slot document`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val carId = "car-1"
            val documents =
                FakeStsDocumentsService(
                    allDocuments =
                        listOf(
                            Document(
                                id = 10,
                                type = CarStsDocumentTypes.FRONT,
                                carId = carId,
                                status = "Rejected",
                            ),
                        ),
                )
            val viewModel =
                CarStsDocumentsViewModelImpl(
                    carId = carId,
                    documents = documents,
                    car = FakeStsCarService(),
                    coroutineScope = testScope,
                )

            viewModel.load()
            advanceUntilIdle()

            viewModel.uploadDocument(
                documentType = CarStsDocumentTypes.FRONT,
                fileBytes = byteArrayOf(1),
                fileName = "sts.jpg",
                contentType = "image/jpeg",
            )
            advanceUntilIdle()

            assertEquals(listOf(10), documents.deletedIds)
            assertEquals(carId, documents.lastUploadCarId)
            assertEquals(1, documents.uploadCount)
            testScope.coroutineContext.cancelChildren()
        }
}

private class FakeStsDocumentsService(
    var allDocuments: List<Document> = emptyList(),
) : Documents {
    val deletedIds = mutableListOf<Int>()
    var uploadCount = 0
    var lastUploadCarId: String? = null

    override suspend fun getDocuments(): List<Document> = allDocuments

    override suspend fun uploadDocument(
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
        documentType: String,
        carId: String?,
    ): Document {
        uploadCount++
        lastUploadCarId = carId
        val doc =
            Document(
                id = 99,
                type = documentType,
                carId = carId,
                status = "Pending",
            )
        allDocuments = allDocuments.filter { it.type != documentType || it.carId != carId } + doc
        return doc
    }

    override suspend fun getDocumentUrl(documentId: Int): String = "https://example.com/$documentId"

    override suspend fun deleteDocument(documentId: Int) {
        deletedIds += documentId
        allDocuments = allDocuments.filter { it.id != documentId }
    }
}

private class FakeStsCarService(
    private val stsSeriesNumber: String? = null,
) : Car {
    override suspend fun getCar(id: String): CarDetailResponse =
        CarDetailResponse(
            id = id,
            stsSeriesNumber = stsSeriesNumber,
            general =
                CarGeneral(
                    brandName = "",
                    modelName = "",
                    vin = "",
                    seats = 0,
                    address = CarAddress(geoLat = 0.0, geoLon = 0.0),
                ),
        )

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
        allowedTravelDestinations: List<String>?,
        geoLat: Double?,
        geoLon: Double?,
        radiusKm: Double?,
        page: Int,
        pageSize: Int,
    ): CarSearchResponse = CarSearchResponse()

    override suspend fun getMyCars(): List<CarItem> = emptyList()

    override suspend fun createCar(request: CarCreateRequest): CarResponse = CarResponse(id = "")

    override suspend fun createOrUpdateCar(
        request: CarCreateRequest,
        carId: String?,
    ): CarResponse = CarResponse(id = carId.orEmpty())

    override suspend fun deleteCar(carId: String) = Unit
}
