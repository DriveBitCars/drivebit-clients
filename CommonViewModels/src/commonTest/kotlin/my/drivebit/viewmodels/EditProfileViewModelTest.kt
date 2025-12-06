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
import kotlin.test.assertTrue

class MockEditProfileUserService : User {
    var shouldThrowError = false
    var errorMessage = "Network error"
    var lastFirstName: String? = null
    var lastLastName: String? = null
    var lastMiddleName: String? = null

    override suspend fun userGet(): UserGetResponse {
        if (shouldThrowError) {
            throw NetworkException(HttpStatusCode.InternalServerError, errorMessage)
        }
        return UserGetResponse(
            id = "test-id",
            firstName = "Test",
            lastName = "User",
            createdAt = "2025-01-01T00:00:00Z",
        )
    }

    override suspend fun updateUser(
        firstName: String?,
        lastName: String?,
        middleName: String?,
    ): UserGetResponse {
        lastFirstName = firstName
        lastLastName = lastName
        lastMiddleName = middleName
        if (shouldThrowError) {
            throw NetworkException(HttpStatusCode.InternalServerError, errorMessage)
        }
        return UserGetResponse(
            id = "test-id",
            firstName = firstName ?: "",
            lastName = lastName ?: "",
            middleName = middleName,
            createdAt = "2025-01-01T00:00:00Z",
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class EditProfileViewModelTest {
    @Test
    fun `initial state should be Initial with provided values`() =
        runTest {
            val testDispatcher = StandardTestDispatcher()
            val scope = CoroutineScope(SupervisorJob() + testDispatcher)
            val mockUserService = MockEditProfileUserService()
            val viewModel =
                EditProfileViewModelImpl(
                    userService = mockUserService,
                    initialFirstName = "John",
                    initialLastName = "Doe",
                    initialMiddleName = "Middle",
                    coroutineScope = scope,
                )

            assertTrue(viewModel.state.value is EditProfileState.Initial)
            val initialState = viewModel.state.value as EditProfileState.Initial
            assertEquals("John", initialState.firstName)
            assertEquals("Doe", initialState.lastName)
            assertEquals("Middle", initialState.middleName)
        }

    @Test
    fun `updateFirstName should update firstName flow`() =
        runTest {
            val testDispatcher = StandardTestDispatcher()
            val scope = CoroutineScope(SupervisorJob() + testDispatcher)
            val mockUserService = MockEditProfileUserService()
            val viewModel =
                EditProfileViewModelImpl(
                    userService = mockUserService,
                    initialFirstName = "",
                    initialLastName = "",
                    initialMiddleName = "",
                    coroutineScope = scope,
                )

            viewModel.updateFirstName("Jane")
            advanceUntilIdle()

            assertTrue(viewModel.state.value is EditProfileState.Initial)
            val initialState = viewModel.state.value as EditProfileState.Initial
            assertEquals("Jane", initialState.firstName)
        }

    @Test
    fun `updateLastName should update lastName flow`() =
        runTest {
            val testDispatcher = StandardTestDispatcher()
            val scope = CoroutineScope(SupervisorJob() + testDispatcher)
            val mockUserService = MockEditProfileUserService()
            val viewModel =
                EditProfileViewModelImpl(
                    userService = mockUserService,
                    initialFirstName = "",
                    initialLastName = "",
                    initialMiddleName = "",
                    coroutineScope = scope,
                )

            viewModel.updateLastName("Smith")
            advanceUntilIdle()

            assertTrue(viewModel.state.value is EditProfileState.Initial)
            val initialState = viewModel.state.value as EditProfileState.Initial
            assertEquals("Smith", initialState.lastName)
        }

    @Test
    fun `updateMiddleName should update middleName flow`() =
        runTest {
            val testDispatcher = StandardTestDispatcher()
            val scope = CoroutineScope(SupervisorJob() + testDispatcher)
            val mockUserService = MockEditProfileUserService()
            val viewModel =
                EditProfileViewModelImpl(
                    userService = mockUserService,
                    initialFirstName = "",
                    initialLastName = "",
                    initialMiddleName = "",
                    coroutineScope = scope,
                )

            viewModel.updateMiddleName("Petrovich")
            advanceUntilIdle()

            assertTrue(viewModel.state.value is EditProfileState.Initial)
            val initialState = viewModel.state.value as EditProfileState.Initial
            assertEquals("Petrovich", initialState.middleName)
        }

    @Test
    fun `save should call updateUser with correct parameters`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockUserService = MockEditProfileUserService()
            val viewModel =
                EditProfileViewModelImpl(
                    userService = mockUserService,
                    initialFirstName = "John",
                    initialLastName = "Doe",
                    initialMiddleName = "Middle",
                    coroutineScope = testScope,
                )

            viewModel.updateFirstName("Jane")
            viewModel.updateLastName("Smith")
            viewModel.updateMiddleName("Petrovich")
            viewModel.save()
            advanceUntilIdle()

            assertEquals("Jane", mockUserService.lastFirstName)
            assertEquals("Smith", mockUserService.lastLastName)
            assertEquals("Petrovich", mockUserService.lastMiddleName)
            assertTrue(viewModel.state.value is EditProfileState.Success)
        }

    @Test
    fun `save should set state to Loading then Success on success`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockUserService = MockEditProfileUserService()
            val viewModel =
                EditProfileViewModelImpl(
                    userService = mockUserService,
                    initialFirstName = "John",
                    initialLastName = "Doe",
                    initialMiddleName = "Middle",
                    coroutineScope = testScope,
                )

            viewModel.save()
            advanceUntilIdle()

            assertTrue(viewModel.state.value is EditProfileState.Success)
        }

    @Test
    fun `save should set state to Error on failure`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockUserService = MockEditProfileUserService()
            mockUserService.shouldThrowError = true
            mockUserService.errorMessage = "Network error"
            val viewModel =
                EditProfileViewModelImpl(
                    userService = mockUserService,
                    initialFirstName = "John",
                    initialLastName = "Doe",
                    initialMiddleName = "Middle",
                    coroutineScope = testScope,
                )

            viewModel.save()
            advanceUntilIdle()

            assertTrue(viewModel.state.value is EditProfileState.Error)
            val errorState = viewModel.state.value as EditProfileState.Error
            assertEquals("Network error", errorState.message)
        }

    @Test
    fun `save should send blank strings as null`() =
        runTest {
            val testDispatcher = StandardTestDispatcher()
            val scope = CoroutineScope(SupervisorJob() + testDispatcher)
            val mockUserService = MockEditProfileUserService()
            val viewModel =
                EditProfileViewModelImpl(
                    userService = mockUserService,
                    initialFirstName = "",
                    initialLastName = "",
                    initialMiddleName = "",
                    coroutineScope = scope,
                )

            viewModel.updateFirstName("")
            viewModel.updateLastName("")
            viewModel.updateMiddleName("")
            viewModel.save()
            advanceUntilIdle()

            assertEquals(null, mockUserService.lastFirstName)
            assertEquals(null, mockUserService.lastLastName)
            assertEquals(null, mockUserService.lastMiddleName)
        }
}
