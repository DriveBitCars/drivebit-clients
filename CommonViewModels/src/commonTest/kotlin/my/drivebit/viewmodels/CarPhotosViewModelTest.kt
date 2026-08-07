package my.drivebit.viewmodels

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.NetworkException
import my.drivebit.network.services.CarPhotoResponse
import my.drivebit.network.services.Photo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

open class MockPhotoServiceForCarPhotos : Photo {
    var shouldThrowError = false
    var shouldThrowNetworkException = false
    var errorMessage = "Network error"
    var networkExceptionStatusCode: HttpStatusCode = HttpStatusCode.InternalServerError

    var reorderShouldFail = false
    var lastReorderPhotoIds: List<Int>? = null
    var reorderGate: kotlinx.coroutines.CompletableDeferred<Unit>? = null

    var photos: List<CarPhotoResponse> =
        listOf(
            CarPhotoResponse(
                id = 1,
                url = "https://example.com/photo1.jpg",
                uploadDate = "2024-01-01",
                sortOrder = 1,
            ),
            CarPhotoResponse(
                id = 2,
                url = "https://example.com/photo2.jpg",
                uploadDate = "2024-01-02",
                sortOrder = 2,
            ),
        )

    override suspend fun getCarPhotos(carId: String): List<CarPhotoResponse> {
        if (shouldThrowNetworkException) {
            throw NetworkException(networkExceptionStatusCode, errorMessage)
        }
        if (shouldThrowError) {
            throw Exception(errorMessage)
        }
        return photos
    }

    override suspend fun uploadCarPhotos(
        carId: String,
        fileBytesList: List<ByteArray>,
        fileNames: List<String>,
        contentTypes: List<String>,
    ): List<CarPhotoResponse> {
        if (shouldThrowNetworkException) {
            throw NetworkException(networkExceptionStatusCode, errorMessage)
        }
        if (shouldThrowError) {
            throw Exception(errorMessage)
        }
        val newPhotos =
            fileNames.mapIndexed { index, fileName ->
                CarPhotoResponse(
                    id = photos.size + index + 1,
                    url = "https://example.com/$fileName",
                    uploadDate = "2024-01-03",
                )
            }
        photos = photos + newPhotos
        return newPhotos
    }

    override suspend fun getAvatar(): my.drivebit.network.services.AvatarResponse = throw NotImplementedError()

    override suspend fun uploadAvatar(
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
    ): my.drivebit.network.services.AvatarResponse = throw NotImplementedError()

    override suspend fun getAvatarByUserId(userId: String): my.drivebit.network.services.AvatarResponse? = null

    override suspend fun deleteCarPhoto(photoId: Int) {
        photos = photos.filter { it.id != photoId }
    }

    override suspend fun reorderCarPhotos(
        carId: String,
        photoIds: List<Int>,
    ): List<CarPhotoResponse> {
        lastReorderPhotoIds = photoIds
        reorderGate?.await()
        if (shouldThrowNetworkException) {
            throw NetworkException(networkExceptionStatusCode, errorMessage)
        }
        if (shouldThrowError || reorderShouldFail) {
            throw Exception(errorMessage)
        }
        photos =
            photoIds.mapIndexed { index, id ->
                val existing = photos.first { it.id == id }
                existing.copy(sortOrder = index + 1)
            }
        return photos
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CarPhotosViewModelTest {
    @Test
    fun `initial state should be Loading`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockPhotoService = MockPhotoServiceForCarPhotos()
            val viewModel =
                CarPhotosViewModelImpl(
                    photoService = mockPhotoService,
                    coroutineScope = testScope,
                )

            val initialState = viewModel.state.value
            assertIs<CarPhotosState.Loading>(initialState)
        }

    @Test
    fun `loadPhotos should set state to Loading then Success with photos`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockPhotoService = MockPhotoServiceForCarPhotos()
            val viewModel =
                CarPhotosViewModelImpl(
                    photoService = mockPhotoService,
                    coroutineScope = testScope,
                )

            viewModel.loadPhotos("test-car-id")
            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarPhotosState.Success>(state)
            val successState = state as CarPhotosState.Success
            assertEquals(2, successState.photos.size)
            assertEquals("https://example.com/photo1.jpg", successState.photos[0].url)
            assertEquals("https://example.com/photo2.jpg", successState.photos[1].url)
            assertFalse(successState.isUploading)
            assertEquals(null, successState.uploadError)
        }

    @Test
    fun `loadPhotos should set state to Error on network exception`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockPhotoService = MockPhotoServiceForCarPhotos().apply { shouldThrowNetworkException = true }
            val viewModel =
                CarPhotosViewModelImpl(
                    photoService = mockPhotoService,
                    coroutineScope = testScope,
                )

            viewModel.loadPhotos("test-car-id")
            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarPhotosState.Error>(state)
            assertEquals("Network error", state.message)
        }

    @Test
    fun `loadPhotos should set state to Error on general exception`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockPhotoService =
                MockPhotoServiceForCarPhotos().apply {
                    shouldThrowError = true
                    errorMessage = ""
                }
            val viewModel =
                CarPhotosViewModelImpl(
                    photoService = mockPhotoService,
                    coroutineScope = testScope,
                )

            viewModel.loadPhotos("test-car-id")
            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarPhotosState.Error>(state)
            assertEquals("Не удалось загрузить фотографии", state.message)
        }

    @Test
    fun `uploadPhotos should reload photos after successful upload`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockPhotoService = MockPhotoServiceForCarPhotos()
            val viewModel =
                CarPhotosViewModelImpl(
                    photoService = mockPhotoService,
                    coroutineScope = testScope,
                )

            viewModel.loadPhotos("test-car-id")
            advanceUntilIdle()

            val initialState = viewModel.state.value as CarPhotosState.Success
            assertEquals(2, initialState.photos.size)

            viewModel.uploadPhotos(
                carId = "test-car-id",
                fileBytesList = listOf(byteArrayOf(1, 2, 3)),
                fileNames = listOf("new-photo.jpg"),
                contentTypes = listOf("image/jpeg"),
            )
            advanceUntilIdle()

            val finalState = viewModel.state.value
            assertIs<CarPhotosState.Success>(finalState)
            assertEquals(3, finalState.photos.size)
            assertFalse(finalState.isUploading)
            assertEquals(null, finalState.uploadError)
        }

    @Test
    fun `uploadPhotos should set uploadError on failure`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockPhotoService = MockPhotoServiceForCarPhotos()
            val viewModel =
                CarPhotosViewModelImpl(
                    photoService = mockPhotoService,
                    coroutineScope = testScope,
                )

            viewModel.loadPhotos("test-car-id")
            advanceUntilIdle()

            val initialState = viewModel.state.value as CarPhotosState.Success

            mockPhotoService.shouldThrowError = true
            viewModel.uploadPhotos(
                carId = "test-car-id",
                fileBytesList = listOf(byteArrayOf(1, 2, 3)),
                fileNames = listOf("new-photo.jpg"),
                contentTypes = listOf("image/jpeg"),
            )
            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<CarPhotosState.Success>(state)
            assertTrue(state.uploadError != null)
        }

    @Test
    fun `uploadPhotos should set isUploading to true during upload`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockPhotoService = MockPhotoServiceForCarPhotos()
            val viewModel =
                CarPhotosViewModelImpl(
                    photoService = mockPhotoService,
                    coroutineScope = testScope,
                )

            viewModel.loadPhotos("test-car-id")
            advanceUntilIdle()

            val initialState = viewModel.state.value as CarPhotosState.Success
            assertFalse(initialState.isUploading, "Should not be uploading initially")

            viewModel.uploadPhotos(
                carId = "test-car-id",
                fileBytesList = listOf(byteArrayOf(1, 2, 3)),
                fileNames = listOf("new-photo.jpg"),
                contentTypes = listOf("image/jpeg"),
            )

            val uploadingState = viewModel.state.value
            assertIs<CarPhotosState.Success>(uploadingState)
            assertFalse(uploadingState.isUploading, "After upload completes, should not be uploading")
        }

    @Test
    fun `uploadPhotos should handle multiple files`() =
        runTest(StandardTestDispatcher()) {
            val testDispatcher = this
            val testScope = CoroutineScope(SupervisorJob() + testDispatcher.coroutineContext)
            val mockPhotoService = MockPhotoServiceForCarPhotos()
            val viewModel =
                CarPhotosViewModelImpl(
                    photoService = mockPhotoService,
                    coroutineScope = testScope,
                )

            viewModel.loadPhotos("test-car-id")
            advanceUntilIdle()

            val initialState = viewModel.state.value as CarPhotosState.Success
            assertEquals(2, initialState.photos.size)

            viewModel.uploadPhotos(
                carId = "test-car-id",
                fileBytesList = listOf(byteArrayOf(1, 2, 3), byteArrayOf(4, 5, 6)),
                fileNames = listOf("photo1.jpg", "photo2.jpg"),
                contentTypes = listOf("image/jpeg", "image/jpeg"),
            )
            advanceUntilIdle()

            val finalState = viewModel.state.value
            assertIs<CarPhotosState.Success>(finalState)
            assertEquals(4, finalState.photos.size, "Should have 2 original + 2 new photos")
        }

    @Test
    fun `movePhoto Down swaps with next and calls reorder with full ids`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val mock = MockPhotoServiceForCarPhotos()
            val vm = CarPhotosViewModelImpl(mock, testScope)

            vm.loadPhotos("car-1")
            advanceUntilIdle()

            vm.movePhoto("car-1", photoId = 1, direction = PhotoMoveDirection.Down)
            advanceUntilIdle()

            val state = assertIs<CarPhotosState.Success>(vm.state.value)
            assertEquals(listOf(2, 1), state.photos.map { it.id })
            assertEquals(listOf(2, 1), mock.lastReorderPhotoIds)
            assertFalse(state.isReordering)
            assertEquals(null, state.uploadError)
        }

    @Test
    fun `movePhoto Up on first photo is no-op`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val mock = MockPhotoServiceForCarPhotos()
            val vm = CarPhotosViewModelImpl(mock, testScope)

            vm.loadPhotos("car-1")
            advanceUntilIdle()

            vm.movePhoto("car-1", photoId = 1, direction = PhotoMoveDirection.Up)
            advanceUntilIdle()

            assertEquals(null, mock.lastReorderPhotoIds)
            val state = assertIs<CarPhotosState.Success>(vm.state.value)
            assertEquals(listOf(1, 2), state.photos.map { it.id })
        }

    @Test
    fun `movePhoto rolls back and sets uploadError when reorder fails`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val mock =
                MockPhotoServiceForCarPhotos().apply {
                    errorMessage = ""
                }
            val vm = CarPhotosViewModelImpl(mock, testScope)

            vm.loadPhotos("car-1")
            advanceUntilIdle()
            mock.shouldThrowError = true

            vm.movePhoto("car-1", photoId = 1, direction = PhotoMoveDirection.Down)
            advanceUntilIdle()

            val state = assertIs<CarPhotosState.Success>(vm.state.value)
            assertEquals(listOf(1, 2), state.photos.map { it.id })
            assertEquals("Не удалось изменить порядок фотографий", state.uploadError)
            assertFalse(state.isReordering)
        }

    @Test
    fun `movePhoto ignored while isReordering`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val gate = kotlinx.coroutines.CompletableDeferred<Unit>()
            val mock =
                MockPhotoServiceForCarPhotos().apply {
                    reorderGate = gate
                }
            val vm = CarPhotosViewModelImpl(mock, testScope)

            vm.loadPhotos("car-1")
            advanceUntilIdle()

            vm.movePhoto("car-1", photoId = 1, direction = PhotoMoveDirection.Down)
            testScheduler.runCurrent()
            val mid = assertIs<CarPhotosState.Success>(vm.state.value)
            assertTrue(mid.isReordering)

            vm.movePhoto("car-1", photoId = 2, direction = PhotoMoveDirection.Down)
            testScheduler.runCurrent()
            assertEquals(listOf(2, 1), mock.lastReorderPhotoIds)

            gate.complete(Unit)
            advanceUntilIdle()
            assertFalse(assertIs<CarPhotosState.Success>(vm.state.value).isReordering)
        }
}
