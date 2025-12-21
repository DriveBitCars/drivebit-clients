package my.drivebit.viewmodels

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.NetworkException
import my.drivebit.network.services.User
import my.drivebit.network.services.UserGetResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MockProfileUserService : User {
    var shouldThrowError = false
    var shouldThrowNetworkException = false
    var errorMessage = "Network error"
    var networkExceptionStatusCode: HttpStatusCode = HttpStatusCode.InternalServerError

    var userResponse: UserGetResponse =
        UserGetResponse(
            id = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            phone = "+1234567890",
            firstName = "John",
            lastName = "Doe",
            middleName = "Middle",
            email = "user@example.com",
            createdAt = "2025-12-02T19:14:47.914Z",
            photos = listOf("photo1.jpg", "photo2.jpg"),
        )

    override suspend fun userGet(): UserGetResponse {
        if (shouldThrowNetworkException) {
            throw NetworkException(networkExceptionStatusCode, errorMessage)
        }
        if (shouldThrowError) {
            throw Exception(errorMessage)
        }
        return userResponse
    }

    override suspend fun updateUser(
        firstName: String?,
        lastName: String?,
        middleName: String?,
    ): UserGetResponse =
        userResponse.copy(
            firstName = firstName ?: userResponse.firstName,
            lastName = lastName ?: userResponse.lastName,
            middleName = middleName ?: userResponse.middleName,
        )

    override suspend fun changeEmail(
        identifier: String,
        code: String,
        newLogin: String,
    ): UserGetResponse {
        if (shouldThrowNetworkException) {
            throw NetworkException(networkExceptionStatusCode, errorMessage)
        }
        if (shouldThrowError) {
            throw Exception(errorMessage)
        }
        return userResponse
    }

    override suspend fun changePhone(
        identifier: String,
        code: String,
        newLogin: String,
    ): UserGetResponse {
        if (shouldThrowNetworkException) {
            throw NetworkException(networkExceptionStatusCode, errorMessage)
        }
        if (shouldThrowError) {
            throw Exception(errorMessage)
        }
        return userResponse
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    @Test
    fun `initial state should be Loading and loadProfile should be called automatically`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockUserService = MockProfileUserService()
            val viewModel =
                ProfileViewModelImpl(
                    userService = mockUserService,
                    coroutineScope = testScope,
                )

            assertIs<ProfileState.Loading>(viewModel.state.value)
            advanceUntilIdle()
            assertIs<ProfileState.Success>(viewModel.state.value)
        }

    @Test
    fun `loadProfile should set state to Success with user data`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockUserService = MockProfileUserService()
            val viewModel =
                ProfileViewModelImpl(
                    userService = mockUserService,
                    coroutineScope = testScope,
                )

            advanceUntilIdle()

            assertIs<ProfileState.Success>(viewModel.state.value)
            val successState = viewModel.state.value as ProfileState.Success
            assertEquals("3fa85f64-5717-4562-b3fc-2c963f66afa6", successState.user.id)
            assertEquals("+1234567890", successState.user.phone)
            assertEquals("John", successState.user.firstName)
            assertEquals("Doe", successState.user.lastName)
            assertEquals("Middle", successState.user.middleName)
            assertEquals("user@example.com", successState.user.email)
            assertEquals("2025-12-02T19:14:47.914Z", successState.user.createdAt)
            assertEquals(2, successState.user.photos.size)
            assertEquals("photo1.jpg", successState.user.photos[0])
            assertEquals("photo2.jpg", successState.user.photos[1])
        }

    @Test
    fun `loadProfile should set state to Error on NetworkException`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockUserService = MockProfileUserService()
            mockUserService.shouldThrowNetworkException = true
            mockUserService.errorMessage = "Unauthorized"
            mockUserService.networkExceptionStatusCode = HttpStatusCode.Unauthorized
            val viewModel =
                ProfileViewModelImpl(
                    userService = mockUserService,
                    coroutineScope = testScope,
                )

            viewModel.loadProfile()
            advanceUntilIdle()

            assertIs<ProfileState.Error>(viewModel.state.value)
            val errorState = viewModel.state.value as ProfileState.Error
            assertEquals("Unauthorized", errorState.message)
        }

    @Test
    fun `loadProfile should set state to Error on generic Exception`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockUserService = MockProfileUserService()
            mockUserService.shouldThrowError = true
            mockUserService.errorMessage = "Connection failed"
            val viewModel =
                ProfileViewModelImpl(
                    userService = mockUserService,
                    coroutineScope = testScope,
                )

            viewModel.loadProfile()
            advanceUntilIdle()

            assertIs<ProfileState.Error>(viewModel.state.value)
            val errorState = viewModel.state.value as ProfileState.Error
            assertEquals("Connection failed", errorState.message)
        }

    @Test
    fun `loadProfile should set state to Error with default message on Exception with empty message`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockUserService = MockProfileUserService()
            mockUserService.shouldThrowError = true
            mockUserService.errorMessage = ""
            val viewModel =
                ProfileViewModelImpl(
                    userService = mockUserService,
                    coroutineScope = testScope,
                )

            viewModel.loadProfile()
            advanceUntilIdle()

            assertIs<ProfileState.Error>(viewModel.state.value)
            val errorState = viewModel.state.value as ProfileState.Error
            assertEquals("Не удалось загрузить профиль", errorState.message)
        }

    @Test
    fun `loadProfile should handle user data with partial fields`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockUserService = MockProfileUserService()
            mockUserService.userResponse =
                UserGetResponse(
                    id = "user-123",
                    phone = "+9876543210",
                    firstName = "Jane",
                    createdAt = "",
                )
            val viewModel =
                ProfileViewModelImpl(
                    userService = mockUserService,
                    coroutineScope = testScope,
                )

            viewModel.loadProfile()
            advanceUntilIdle()

            assertIs<ProfileState.Success>(viewModel.state.value)
            val successState = viewModel.state.value as ProfileState.Success
            assertEquals("user-123", successState.user.id)
            assertEquals("+9876543210", successState.user.phone)
            assertEquals("Jane", successState.user.firstName)
            assertEquals(null, successState.user.lastName)
            assertEquals(null, successState.user.middleName)
            assertEquals(null, successState.user.email)
            assertEquals("", successState.user.createdAt)
            assertTrue(successState.user.photos.isEmpty())
        }

    @Test
    fun `loadProfile should set Loading state before making request`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockUserService = MockProfileUserService()
            val viewModel =
                ProfileViewModelImpl(
                    userService = mockUserService,
                    coroutineScope = testScope,
                )

            viewModel.loadProfile()

            assertIs<ProfileState.Loading>(viewModel.state.value)

            advanceUntilIdle()

            assertIs<ProfileState.Success>(viewModel.state.value)
        }
}
