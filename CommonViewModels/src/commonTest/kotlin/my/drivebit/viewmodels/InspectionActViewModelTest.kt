package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.BookingInspectionActDownloadDto
import my.drivebit.network.services.BookingInspectionActDto
import my.drivebit.network.services.BookingInspectionActPhotoDto
import my.drivebit.network.services.InspectionAct
import my.drivebit.network.services.InspectionActStatus
import my.drivebit.network.services.InspectionActType
import my.drivebit.network.services.InspectionPhotoKind
import my.drivebit.network.services.UpdateInspectionCommentRequest
import my.drivebit.network.services.UpdateInspectionMetricsRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

private class InspectionActFake : InspectionAct {
    var act = sampleAct()
    var download = sampleDownload()
    var getCalls = 0
    var openOrCreateCalls = 0
    var metricsRequest: UpdateInspectionMetricsRequest? = null
    var commentRequest: UpdateInspectionCommentRequest? = null
    var deletedPhotoId: String? = null
    var uploadedKind: InspectionPhotoKind? = null
    var signedAsOwnerCalls = 0
    var signedAsRenterCalls = 0
    var failMutations = false

    override suspend fun get(
        bookingId: String,
        type: InspectionActType,
    ): BookingInspectionActDto {
        getCalls++
        return act
    }

    override suspend fun openOrCreate(
        bookingId: String,
        type: InspectionActType,
    ): BookingInspectionActDto {
        openOrCreateCalls++
        return act
    }

    override suspend fun updateMetrics(
        bookingId: String,
        type: InspectionActType,
        request: UpdateInspectionMetricsRequest,
    ): BookingInspectionActDto {
        if (failMutations) error("metrics failed")
        metricsRequest = request
        return act.copy(fuelRemaining = request.fuelRemaining, mileage = request.mileage)
    }

    override suspend fun updateComment(
        bookingId: String,
        type: InspectionActType,
        request: UpdateInspectionCommentRequest,
    ): BookingInspectionActDto {
        if (failMutations) error("comment failed")
        commentRequest = request
        return act.copy(renterComment = request.comment)
    }

    override suspend fun uploadPhoto(
        bookingId: String,
        type: InspectionActType,
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
        kind: InspectionPhotoKind,
    ): BookingInspectionActPhotoDto {
        if (failMutations) error("upload failed")
        uploadedKind = kind
        return samplePhoto()
    }

    override suspend fun deletePhoto(
        bookingId: String,
        type: InspectionActType,
        photoId: String,
    ) {
        if (failMutations) error("delete failed")
        deletedPhotoId = photoId
    }

    override suspend fun signAsOwner(
        bookingId: String,
        type: InspectionActType,
    ): BookingInspectionActDto {
        if (failMutations) error("owner sign failed")
        signedAsOwnerCalls++
        return act.copy(isSignedByOwner = true)
    }

    override suspend fun signAsRenter(
        bookingId: String,
        type: InspectionActType,
    ): BookingInspectionActDto {
        if (failMutations) error("renter sign failed")
        signedAsRenterCalls++
        return act.copy(isSignedByRenter = true)
    }

    override suspend fun download(
        bookingId: String,
        type: InspectionActType,
    ): BookingInspectionActDownloadDto = download
}

class InspectionActViewModelTest {
    @Test
    fun `load opens or creates act and exposes form values`() =
        runTest {
            val api = InspectionActFake()
            val viewModel = viewModel(api)

            viewModel.load()
            advanceUntilIdle()

            val ready = assertIs<InspectionActUiState.Ready>(viewModel.state.value)
            assertEquals(1, api.openOrCreateCalls)
            assertEquals("75", ready.fuelInput)
            assertEquals("120500", ready.mileageInput)
            assertEquals("Есть царапина", ready.commentInput)
        }

    @Test
    fun `save metrics sends form values and updates act`() =
        runTest {
            val api = InspectionActFake()
            val viewModel = viewModel(api)
            viewModel.load()
            advanceUntilIdle()

            viewModel.setFuelInput("80")
            viewModel.setMileageInput("121000")
            viewModel.saveMetrics()
            advanceUntilIdle()

            assertEquals(UpdateInspectionMetricsRequest(80, 121000), api.metricsRequest)
            assertEquals(80, assertIs<InspectionActUiState.Ready>(viewModel.state.value).act.fuelRemaining)
        }

    @Test
    fun `save comment sends nullable value and upload forwards kind`() =
        runTest {
            val api = InspectionActFake()
            val viewModel = viewModel(api)
            viewModel.load()
            advanceUntilIdle()

            viewModel.setCommentInput("")
            viewModel.saveComment()
            viewModel.uploadPhoto(byteArrayOf(1), "dashboard.jpg", "image/jpeg", InspectionPhotoKind.Dashboard)
            advanceUntilIdle()

            assertEquals(UpdateInspectionCommentRequest(null), api.commentRequest)
            assertEquals(InspectionPhotoKind.Dashboard, api.uploadedKind)
        }

    @Test
    fun `delete photo refreshes act after successful deletion`() =
        runTest {
            val api = InspectionActFake()
            val viewModel = viewModel(api)
            viewModel.load()
            advanceUntilIdle()

            viewModel.deletePhoto("photo-1")
            advanceUntilIdle()

            assertEquals("photo-1", api.deletedPhotoId)
            assertEquals(1, api.getCalls)
        }

    @Test
    fun `sign and download expose updated state and PDF effect`() =
        runTest {
            val api = InspectionActFake()
            val viewModel = viewModel(api)
            viewModel.load()
            advanceUntilIdle()
            val effects = mutableListOf<InspectionActEffect>()
            val collectJob =
                launch {
                    viewModel.effects.collect { effects += it }
                }

            viewModel.signAsOwner()
            viewModel.signAsRenter()
            viewModel.downloadPdf()
            advanceUntilIdle()
            collectJob.cancel()

            assertEquals(1, api.signedAsOwnerCalls)
            assertEquals(1, api.signedAsRenterCalls)
            assertTrue(assertIs<InspectionActUiState.Ready>(viewModel.state.value).act.isSignedByRenter)
            assertTrue(effects.contains(InspectionActEffect.OpenPdf(api.download)))
        }

    @Test
    fun `failed mutation preserves previous act and exposes error`() =
        runTest {
            val api = InspectionActFake().apply { failMutations = true }
            val viewModel = viewModel(api)
            viewModel.load()
            advanceUntilIdle()

            viewModel.saveMetrics()
            advanceUntilIdle()

            val state = assertIs<InspectionActUiState.Error>(viewModel.state.value)
            assertEquals(75, state.previousAct?.fuelRemaining)
            assertFalse(viewModel.error.value.isNullOrBlank())
        }

    private fun CoroutineScope.viewModel(api: InspectionActFake) =
        InspectionActViewModelImpl(
            inspectionAct = api,
            bookingId = "booking-1",
            type = InspectionActType.Handover,
            coroutineScope = CoroutineScope(SupervisorJob() + coroutineContext),
        )
}

private fun sampleAct() =
    BookingInspectionActDto(
        id = "act-1",
        bookingId = "booking-1",
        type = InspectionActType.Handover,
        actNumber = 42,
        fuelRemaining = 75,
        mileage = 120500,
        ownerComment = "Есть царапина",
        renterComment = "Принял",
        status = InspectionActStatus.Draft,
        canEditOwnerFields = true,
        canEditRenterFields = true,
        canSignAsOwner = true,
        canSignAsRenter = true,
        createdAt = "2026-08-29T08:00:00Z",
        updatedAt = "2026-08-29T08:00:00Z",
    )

private fun samplePhoto() =
    BookingInspectionActPhotoDto(
        id = "photo-1",
        authorId = "author-1",
        kind = InspectionPhotoKind.Car,
        uploadedAt = "2026-08-29T08:30:00Z",
    )

private fun sampleDownload() =
    BookingInspectionActDownloadDto(
        actId = "act-1",
        actNumber = 42,
        type = InspectionActType.Handover,
        fileName = "act.pdf",
        downloadUrl = "https://example.com/act.pdf",
        urlExpiresAt = "2026-08-29T10:00:00Z",
    )
