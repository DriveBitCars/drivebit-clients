package my.drivebit.viewmodels

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.NetworkException
import my.drivebit.network.services.AvatarResponse
import my.drivebit.network.services.CarPhotoResponse
import my.drivebit.network.services.Photo
import my.drivebit.repositories.AvatarRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

private class MockPhoto : Photo {
    var shouldThrowError = false
    var shouldThrowNetworkException = false
    var errorMessage = "Network error"
    var networkExceptionStatusCode: HttpStatusCode = HttpStatusCode.InternalServerError
    var lastFileBytes: ByteArray? = null
    var lastFileName: String? = null
    var lastContentType: String? = null

    override suspend fun getAvatar(): AvatarResponse {
        if (shouldThrowNetworkException) {
            throw NetworkException(networkExceptionStatusCode, errorMessage)
        }
        if (shouldThrowError) {
            throw Exception(errorMessage)
        }
        return AvatarResponse(url = "https://example.com/avatar.jpg")
    }

    override suspend fun getAvatarByUserId(userId: String): AvatarResponse? = getAvatar()

    override suspend fun uploadAvatar(
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
    ): AvatarResponse {
        lastFileBytes = fileBytes
        lastFileName = fileName
        lastContentType = contentType
        if (shouldThrowNetworkException) {
            throw NetworkException(networkExceptionStatusCode, errorMessage)
        }
        if (shouldThrowError) {
            throw Exception(errorMessage)
        }
        return AvatarResponse(url = "https://example.com/avatar.jpg")
    }

    override suspend fun getCarPhotos(carId: String): List<CarPhotoResponse> = emptyList()

    override suspend fun uploadCarPhotos(
        carId: String,
        fileBytesList: List<ByteArray>,
        fileNames: List<String>,
        contentTypes: List<String>,
    ): List<CarPhotoResponse> = emptyList()

    override suspend fun deleteCarPhoto(photoId: Int) {
        // Mock implementation
    }

    override suspend fun reorderCarPhotos(
        carId: String,
        photoIds: List<Int>,
    ): List<CarPhotoResponse> = emptyList()
}

private class MockAvatarRepository : AvatarRepository {
    private val _avatarUrl = kotlinx.coroutines.flow.MutableStateFlow("images/menu/user.svg")
    override val avatarUrl: kotlinx.coroutines.flow.Flow<String> = _avatarUrl
    var clearCacheCalled = false

    override fun clearCache() {
        clearCacheCalled = true
    }

    override suspend fun refresh() {
        // No-op for testing
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AvatarUploadViewModelTest {
    @Test
    fun `initial state should be Idle`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockPhoto = MockPhoto()
            val mockRepository = MockAvatarRepository()
            val viewModel =
                AvatarUploadViewModelImpl(
                    photo = mockPhoto,
                    avatarRepository = mockRepository,
                    coroutineScope = testScope,
                )

            assertIs<AvatarUploadState.Idle>(viewModel.state.value)
            testScope.coroutineContext.cancelChildren()
        }

    @Test
    fun `uploadAvatar should set state to Uploading then Success`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockPhoto = MockPhoto()
            val mockRepository = MockAvatarRepository()
            val viewModel =
                AvatarUploadViewModelImpl(
                    photo = mockPhoto,
                    avatarRepository = mockRepository,
                    coroutineScope = testScope,
                )

            val fileBytes = byteArrayOf(1, 2, 3, 4, 5)
            val fileName = "avatar.jpg"
            val contentType = "image/jpeg"

            viewModel.uploadAvatar(fileBytes, fileName, contentType)

            advanceUntilIdle()

            assertIs<AvatarUploadState.Success>(viewModel.state.value)
            assertEquals(fileBytes, mockPhoto.lastFileBytes)
            assertEquals(fileName, mockPhoto.lastFileName)
            assertEquals(contentType, mockPhoto.lastContentType)
            assertTrue(mockRepository.clearCacheCalled)
            testScope.coroutineContext.cancelChildren()
        }

    @Test
    fun `uploadAvatar should set state to Error on network exception`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockPhoto =
                MockPhoto().apply {
                    shouldThrowNetworkException = true
                    errorMessage = "File too large"
                }
            val mockRepository = MockAvatarRepository()
            val viewModel =
                AvatarUploadViewModelImpl(
                    photo = mockPhoto,
                    avatarRepository = mockRepository,
                    coroutineScope = testScope,
                )

            val fileBytes = byteArrayOf(1, 2, 3)
            viewModel.uploadAvatar(fileBytes, "avatar.jpg", "image/jpeg")

            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<AvatarUploadState.Error>(state)
            assertEquals("File too large", state.message)
            assertTrue(!mockRepository.clearCacheCalled)
            testScope.coroutineContext.cancelChildren()
        }

    @Test
    fun `uploadAvatar should set state to Error on generic exception`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockPhoto =
                MockPhoto().apply {
                    shouldThrowError = true
                    errorMessage = "Upload failed"
                }
            val mockRepository = MockAvatarRepository()
            val viewModel =
                AvatarUploadViewModelImpl(
                    photo = mockPhoto,
                    avatarRepository = mockRepository,
                    coroutineScope = testScope,
                )

            val fileBytes = byteArrayOf(1, 2, 3)
            viewModel.uploadAvatar(fileBytes, "avatar.jpg", "image/jpeg")

            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<AvatarUploadState.Error>(state)
            assertEquals("Upload failed", state.message)
            assertTrue(!mockRepository.clearCacheCalled)
            testScope.coroutineContext.cancelChildren()
        }

    @Test
    fun `uploadAvatar should set state to Error with default message when exception has no message`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockPhoto =
                MockPhoto().apply {
                    shouldThrowError = true
                    errorMessage = ""
                }
            val mockRepository = MockAvatarRepository()
            val viewModel =
                AvatarUploadViewModelImpl(
                    photo = mockPhoto,
                    avatarRepository = mockRepository,
                    coroutineScope = testScope,
                )

            val fileBytes = byteArrayOf(1, 2, 3)
            viewModel.uploadAvatar(fileBytes, "avatar.jpg", "image/jpeg")

            advanceUntilIdle()

            val state = viewModel.state.value
            assertIs<AvatarUploadState.Error>(state)
            assertEquals("Ошибка при загрузке файла", state.message)
            testScope.coroutineContext.cancelChildren()
        }
}
