package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.Booking
import my.drivebit.network.services.BookingDTO
import my.drivebit.network.services.BookingInspectionActDownloadDto
import my.drivebit.network.services.BookingInspectionActDto
import my.drivebit.network.services.BookingInspectionActPhotoDto
import my.drivebit.network.services.CheckBookingAvailabilityRequest
import my.drivebit.network.services.CheckBookingAvailabilityResponse
import my.drivebit.network.services.CreateBookingRequest
import my.drivebit.network.services.GetBookingContractResult
import my.drivebit.network.services.InspectionAct
import my.drivebit.network.services.InspectionActStatus
import my.drivebit.network.services.InspectionActType
import my.drivebit.network.services.InspectionActViewerRole
import my.drivebit.network.services.InspectionPhotoKind
import my.drivebit.network.services.UpdateInspectionCommentRequest
import my.drivebit.network.services.UpdateInspectionMetricsRequest
import my.drivebit.network.services.User
import my.drivebit.network.services.UserGetResponse
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
    val uploadedKinds = mutableListOf<InspectionPhotoKind>()
    var uploadCallCount = 0
    var failUploadAtCall: Int? = null
    var signedAsOwnerCalls = 0
    var signedAsRenterCalls = 0
    var failMutations = false
    val callOrder = mutableListOf<String>()

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
        callOrder += "metrics"
        metricsRequest = request
        act = act.copy(fuelRemaining = request.fuelRemaining, mileage = request.mileage)
        return act
    }

    override suspend fun updateComment(
        bookingId: String,
        type: InspectionActType,
        request: UpdateInspectionCommentRequest,
    ): BookingInspectionActDto {
        if (failMutations) error("comment failed")
        callOrder += "comment"
        commentRequest = request
        act = act.copy(ownerComment = request.comment)
        return act
    }

    override suspend fun uploadPhoto(
        bookingId: String,
        type: InspectionActType,
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
        kind: InspectionPhotoKind,
    ): BookingInspectionActPhotoDto {
        uploadCallCount++
        if (failMutations || failUploadAtCall == uploadCallCount) error("upload failed")
        uploadedKind = kind
        uploadedKinds += kind
        return samplePhoto(id = "photo-$uploadCallCount", kind = kind)
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
        callOrder += "signOwner"
        signedAsOwnerCalls++
        act =
            act.copy(
                isSignedByOwner = true,
                canEditOwnerFields = false,
                canSignAsOwner = false,
            )
        return act
    }

    override suspend fun signAsRenter(
        bookingId: String,
        type: InspectionActType,
    ): BookingInspectionActDto {
        if (failMutations) error("renter sign failed")
        callOrder += "signRenter"
        signedAsRenterCalls++
        act =
            act.copy(
                isSignedByRenter = true,
                canEditRenterFields = false,
                canSignAsRenter = false,
            )
        return act
    }

    override suspend fun download(
        bookingId: String,
        type: InspectionActType,
    ): BookingInspectionActDownloadDto = download
}

private class BookingFake(
    private val ownerId: String = "owner-1",
    private val renterId: String = "renter-1",
) : Booking {
    override suspend fun calculate(request: CheckBookingAvailabilityRequest): CheckBookingAvailabilityResponse =
        error("not used")

    override suspend fun getMyAsRenter(): List<BookingDTO> = error("not used")

    override suspend fun getMyAsOwner(): List<BookingDTO> = error("not used")

    override suspend fun getById(bookingId: String): BookingDTO = sampleBooking(bookingId, ownerId, renterId)

    override suspend fun createAsRenter(request: CreateBookingRequest): BookingDTO = error("not used")

    override suspend fun confirmAsOwner(bookingId: String) = error("not used")

    override suspend fun declineAsOwner(bookingId: String) = error("not used")

    override suspend fun signContractAsOwner(bookingId: String): BookingDTO = error("not used")

    override suspend fun signContractAsRenter(bookingId: String): BookingDTO = error("not used")

    override suspend fun getContract(bookingId: String): GetBookingContractResult = error("not used")
}

private class UserFake(
    private val userId: String = "owner-1",
) : User {
    override suspend fun userGet(): UserGetResponse =
        UserGetResponse(
            id = userId,
            createdAt = "2026-08-29T08:00:00Z",
        )

    override suspend fun getUserById(userId: String): UserGetResponse = error("not used")

    override suspend fun updateUser(
        firstName: String?,
        lastName: String?,
        middleName: String?,
    ): UserGetResponse = error("not used")

    override suspend fun changeEmail(
        identifier: String,
        code: String,
        newLogin: String,
    ): UserGetResponse = error("not used")

    override suspend fun changePhone(
        identifier: String,
        code: String,
        newLogin: String,
    ): UserGetResponse = error("not used")
}

class InspectionActViewModelTest {
    @Test
    fun `load opens or creates act and exposes form values for owner`() =
        runTest {
            val api = InspectionActFake()
            val viewModel = viewModel(api, userId = "owner-1")

            viewModel.load()
            advanceUntilIdle()

            val ready = assertIs<InspectionActUiState.Ready>(viewModel.state.value)
            assertEquals(1, api.openOrCreateCalls)
            assertEquals(InspectionActViewerRole.Owner, ready.viewerRole)
            assertEquals("75", ready.fuelInput)
            assertEquals("120500", ready.mileageInput)
            assertEquals("Есть царапина", ready.commentInput)
            assertEquals("Иван Владельцев", ready.ownerName)
            assertEquals("Пётр Арендаторов", ready.renterName)
        }

    @Test
    fun `load exposes owner and renter names from booking for party labels`() =
        runTest {
            val api = InspectionActFake()
            val viewModel = viewModel(api, userId = "renter-1")

            viewModel.load()
            advanceUntilIdle()

            val ready = assertIs<InspectionActUiState.Ready>(viewModel.state.value)
            assertEquals("Иван Владельцев", ready.ownerName)
            assertEquals("Пётр Арендаторов", ready.renterName)
        }

    @Test
    fun `sign as owner persists metrics and comment from form before signing`() =
        runTest {
            val api = InspectionActFake()
            val viewModel = viewModel(api, userId = "owner-1")
            viewModel.load()
            advanceUntilIdle()

            viewModel.setFuelInput("80")
            viewModel.setMileageInput("121000")
            viewModel.setCommentInput("Новый комментарий")
            viewModel.sign()
            advanceUntilIdle()

            assertEquals(UpdateInspectionMetricsRequest(80, 121000), api.metricsRequest)
            assertEquals(UpdateInspectionCommentRequest("Новый комментарий"), api.commentRequest)
            assertEquals(listOf("metrics", "comment", "signOwner"), api.callOrder)
            assertEquals(1, api.signedAsOwnerCalls)
            val ready = assertIs<InspectionActUiState.Ready>(viewModel.state.value)
            assertFalse(ready.act.canEditOwnerFields)
            assertTrue(ready.act.isSignedByOwner)
        }

    @Test
    fun `sign as owner rejects invalid metrics without api calls`() =
        runTest {
            val api = InspectionActFake()
            val viewModel = viewModel(api, userId = "owner-1")
            viewModel.load()
            advanceUntilIdle()

            viewModel.setFuelInput("abc")
            viewModel.setMileageInput("121000")
            viewModel.sign()
            advanceUntilIdle()

            assertEquals(null, api.metricsRequest)
            assertEquals(0, api.signedAsOwnerCalls)
            assertIs<InspectionActUiState.Error>(viewModel.state.value)
        }

    @Test
    fun `sign as renter persists comment from form before signing`() =
        runTest {
            val api = InspectionActFake()
            val viewModel = viewModel(api, userId = "renter-1")
            viewModel.load()
            advanceUntilIdle()

            viewModel.setCommentInput("Комментарий арендатора")
            viewModel.sign()
            advanceUntilIdle()

            assertEquals(null, api.metricsRequest)
            assertEquals(UpdateInspectionCommentRequest("Комментарий арендатора"), api.commentRequest)
            assertEquals(listOf("comment", "signRenter"), api.callOrder)
            assertEquals(1, api.signedAsRenterCalls)
            val ready = assertIs<InspectionActUiState.Ready>(viewModel.state.value)
            assertFalse(ready.act.canEditRenterFields)
            assertTrue(ready.act.isSignedByRenter)
        }

    @Test
    fun `upload photo uses provided kind`() =
        runTest {
            val api = InspectionActFake()
            val viewModel = viewModel(api, userId = "renter-1")
            viewModel.load()
            advanceUntilIdle()

            viewModel.uploadPhoto(byteArrayOf(1), "car.jpg", "image/jpeg", InspectionPhotoKind.Car)
            advanceUntilIdle()
            assertEquals(InspectionPhotoKind.Car, api.uploadedKind)

            viewModel.uploadPhoto(byteArrayOf(2), "dash.jpg", "image/jpeg", InspectionPhotoKind.Dashboard)
            advanceUntilIdle()
            assertEquals(InspectionPhotoKind.Dashboard, api.uploadedKind)
            assertEquals(
                listOf(InspectionPhotoKind.Car, InspectionPhotoKind.Dashboard),
                api.uploadedKinds,
            )
        }

    @Test
    fun `upload photos uploads all files and reports status`() =
        runTest {
            val api = InspectionActFake()
            val viewModel = viewModel(api, userId = "renter-1")
            viewModel.load()
            advanceUntilIdle()

            viewModel.uploadPhotos(
                files =
                    listOf(
                        InspectionActPhotoFile(byteArrayOf(1), "a.jpg", "image/jpeg"),
                        InspectionActPhotoFile(byteArrayOf(2), "b.jpg", "image/jpeg"),
                        InspectionActPhotoFile(byteArrayOf(3), "c.jpg", "image/jpeg"),
                    ),
                kind = InspectionPhotoKind.Car,
            )
            advanceUntilIdle()

            assertEquals(3, api.uploadCallCount)
            assertEquals("Загружено фото: 3", viewModel.photoUploadStatus.value)
            val ready = assertIs<InspectionActUiState.Ready>(viewModel.state.value)
            assertEquals(3, ready.act.photos?.size)
        }

    @Test
    fun `upload photos reports partial status when some files fail`() =
        runTest {
            val api = InspectionActFake().apply { failUploadAtCall = 2 }
            val viewModel = viewModel(api, userId = "renter-1")
            viewModel.load()
            advanceUntilIdle()

            viewModel.uploadPhotos(
                files =
                    listOf(
                        InspectionActPhotoFile(byteArrayOf(1), "a.jpg", "image/jpeg"),
                        InspectionActPhotoFile(byteArrayOf(2), "b.jpg", "image/jpeg"),
                        InspectionActPhotoFile(byteArrayOf(3), "c.jpg", "image/jpeg"),
                    ),
                kind = InspectionPhotoKind.Dashboard,
            )
            advanceUntilIdle()

            assertEquals(3, api.uploadCallCount)
            assertEquals("Загружено 2 из 3", viewModel.photoUploadStatus.value)
            val ready = assertIs<InspectionActUiState.Ready>(viewModel.state.value)
            assertEquals(2, ready.act.photos?.size)
            assertTrue(ready.act.photos.orEmpty().all { it.kind == InspectionPhotoKind.Dashboard })
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
    fun `download exposes PDF effect`() =
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

            viewModel.downloadPdf()
            advanceUntilIdle()
            collectJob.cancel()

            assertTrue(effects.contains(InspectionActEffect.OpenPdf(api.download)))
        }

    @Test
    fun `failed sign preserves previous act and exposes error`() =
        runTest {
            val api = InspectionActFake().apply { failMutations = true }
            val viewModel = viewModel(api, userId = "owner-1")
            viewModel.load()
            advanceUntilIdle()

            viewModel.sign()
            advanceUntilIdle()

            val state = assertIs<InspectionActUiState.Error>(viewModel.state.value)
            assertEquals(75, state.previousAct?.fuelRemaining)
            assertFalse(viewModel.error.value.isNullOrBlank())
        }

    private fun CoroutineScope.viewModel(
        api: InspectionActFake,
        userId: String = "owner-1",
    ) = InspectionActViewModelImpl(
        inspectionAct = api,
        booking = BookingFake(ownerId = "owner-1", renterId = "renter-1"),
        user = UserFake(userId = userId),
        bookingId = "booking-1",
        type = InspectionActType.Handover,
        coroutineScope = CoroutineScope(SupervisorJob() + coroutineContext),
    )
}

private fun sampleBooking(
    bookingId: String,
    ownerId: String,
    renterId: String,
) = BookingDTO(
    id = bookingId,
    carId = "car-1",
    renterId = renterId,
    renterName = "Пётр Арендаторов",
    ownerId = ownerId,
    ownerName = "Иван Владельцев",
    startAt = "2026-09-01T07:00:00Z",
    endAt = "2026-09-02T07:00:00Z",
    totalAmount = 30.0,
    status = "Paid",
    createdAt = "2026-09-01T05:53:42Z",
)

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

private fun samplePhoto(
    id: String = "photo-1",
    kind: InspectionPhotoKind = InspectionPhotoKind.Car,
) = BookingInspectionActPhotoDto(
    id = id,
    authorId = "author-1",
    kind = kind,
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
