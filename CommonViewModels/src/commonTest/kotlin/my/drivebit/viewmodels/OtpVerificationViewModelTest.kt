package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.Auth
import my.drivebit.network.services.VerifyOtpResponse
import my.drivebit.shared.storage.Storage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MockStorageForOtp : Storage {
    private var token: String? = null
    private var refreshToken: String? = null

    override fun isLogined(): Boolean = token != null && token!!.isNotEmpty()

    override fun saveToken(token: String) {
        this.token = token
    }

    override fun getToken(): String? = token

    override fun saveRefreshToken(refreshToken: String) {
        this.refreshToken = refreshToken
    }

    override fun getRefreshToken(): String? = refreshToken

    override fun logout() {
        token = null
        refreshToken = null
    }

    fun clear() {
        token = null
        refreshToken = null
    }
}

class MockAuthForOtp : Auth {
    var shouldThrowError = false
    var errorMessage = "Network error"
    var lastVerifyIdentifier: String? = null
    var lastVerifyCode: String? = null

    override suspend fun createOtp(login: String): my.drivebit.network.services.CreateOtpResponse =
        throw NotImplementedError("Not used in OtpVerificationViewModel")

    override suspend fun verifyOtp(
        identifier: String,
        code: String,
    ): VerifyOtpResponse {
        lastVerifyIdentifier = identifier
        lastVerifyCode = code
        if (shouldThrowError) {
            throw Exception(errorMessage)
        }
        return VerifyOtpResponse(
            accessToken =
                my.drivebit.network.services.AccessTokenDTO(
                    token = "test-access-token",
                    expiresAt = null,
                ),
            refreshToken =
                my.drivebit.network.services.RefreshTokenDTO(
                    token = "test-refresh-token",
                    userId = null,
                    expiresAt = null,
                    createdAt = null,
                ),
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class OtpVerificationViewModelTest {
    @Test
    fun `initial state should be Idle`() =
        runTest {
            val mockAuth = MockAuthForOtp()
            val mockStorage = MockStorageForOtp()
            val viewModel =
                OtpVerificationViewModel(
                    auth = mockAuth,
                    storage = mockStorage,
                    identifier = "test-identifier",
                )

            assertTrue(viewModel.state.value is OtpVerificationState.Idle)
            assertEquals("", viewModel.code.value)
        }

    @Test
    fun `updateCode should update code and clear error`() =
        runTest {
            val mockAuth = MockAuthForOtp()
            val mockStorage = MockStorageForOtp()
            val viewModel =
                OtpVerificationViewModel(
                    auth = mockAuth,
                    storage = mockStorage,
                    identifier = "test-identifier",
                )

            viewModel.updateCode("123456")
            assertEquals("123456", viewModel.code.value)

            viewModel.updateCode("abc123def456")
            assertEquals("123456", viewModel.code.value)
        }

    @Test
    fun `updateCode should limit to 6 digits`() =
        runTest {
            val mockAuth = MockAuthForOtp()
            val mockStorage = MockStorageForOtp()
            val viewModel =
                OtpVerificationViewModel(
                    auth = mockAuth,
                    storage = mockStorage,
                    identifier = "test-identifier",
                )

            viewModel.updateCode("1234567890")
            assertEquals("123456", viewModel.code.value)
        }

    @Test
    fun `verifyOtp should set error if code length is not 6`() =
        runTest {
            val mockAuth = MockAuthForOtp()
            val mockStorage = MockStorageForOtp()
            val viewModel =
                OtpVerificationViewModel(
                    auth = mockAuth,
                    storage = mockStorage,
                    identifier = "test-identifier",
                )

            viewModel.updateCode("12345")
            viewModel.verifyOtp()

            assertTrue(viewModel.state.value is OtpVerificationState.Error)
            val errorState = viewModel.state.value as OtpVerificationState.Error
            assertEquals("Введите 6-значный код", errorState.message)
        }

    @Test
    fun `verifyOtp should call auth service with correct parameters`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockAuth = MockAuthForOtp()
            val mockStorage = MockStorageForOtp()
            val identifier = "test-session-id"
            val viewModel =
                OtpVerificationViewModel(
                    auth = mockAuth,
                    storage = mockStorage,
                    identifier = identifier,
                    coroutineScope = testScope,
                )

            viewModel.updateCode("123456")
            viewModel.verifyOtp()
            advanceUntilIdle()

            assertEquals(identifier, mockAuth.lastVerifyIdentifier)
            assertEquals("123456", mockAuth.lastVerifyCode)
        }

    @Test
    fun `verifyOtp should save token and refreshToken to storage on success`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockAuth = MockAuthForOtp()
            val mockStorage = MockStorageForOtp()
            val viewModel =
                OtpVerificationViewModel(
                    auth = mockAuth,
                    storage = mockStorage,
                    identifier = "test-identifier",
                    coroutineScope = testScope,
                )

            viewModel.updateCode("123456")
            viewModel.verifyOtp()
            advanceUntilIdle()

            assertEquals("test-access-token", mockStorage.getToken())
            assertEquals("test-refresh-token", mockStorage.getRefreshToken())
            assertTrue(mockStorage.isLogined())
        }

    @Test
    fun `verifyOtp should set state to Success on success`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockAuth = MockAuthForOtp()
            val mockStorage = MockStorageForOtp()
            val viewModel =
                OtpVerificationViewModel(
                    auth = mockAuth,
                    storage = mockStorage,
                    identifier = "test-identifier",
                    coroutineScope = testScope,
                )

            viewModel.updateCode("123456")
            viewModel.verifyOtp()
            advanceUntilIdle()

            assertTrue(viewModel.state.value is OtpVerificationState.Success)
        }

    @Test
    fun `verifyOtp should set state to Error on failure`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockAuth = MockAuthForOtp()
            mockAuth.shouldThrowError = true
            mockAuth.errorMessage = "Invalid OTP code"
            val mockStorage = MockStorageForOtp()
            val viewModel =
                OtpVerificationViewModel(
                    auth = mockAuth,
                    storage = mockStorage,
                    identifier = "test-identifier",
                    coroutineScope = testScope,
                )

            viewModel.updateCode("123456")
            viewModel.verifyOtp()
            advanceUntilIdle()

            assertTrue(viewModel.state.value is OtpVerificationState.Error)
            val errorState = viewModel.state.value as OtpVerificationState.Error
            assertEquals("Invalid OTP code", errorState.message)
        }

    @Test
    fun `verifyOtp should handle exception with empty message`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockAuth = MockAuthForOtp()
            mockAuth.shouldThrowError = true
            mockAuth.errorMessage = ""
            val mockStorage = MockStorageForOtp()
            val viewModel =
                OtpVerificationViewModel(
                    auth = mockAuth,
                    storage = mockStorage,
                    identifier = "test-identifier",
                    coroutineScope = testScope,
                )

            viewModel.updateCode("123456")
            viewModel.verifyOtp()
            advanceUntilIdle()

            assertTrue(viewModel.state.value is OtpVerificationState.Error)
            val errorState = viewModel.state.value as OtpVerificationState.Error
            assertEquals("Произошла ошибка", errorState.message)
        }

    @Test
    fun `clearError should reset state to Idle`() =
        runTest {
            val mockAuth = MockAuthForOtp()
            val mockStorage = MockStorageForOtp()
            val viewModel =
                OtpVerificationViewModel(
                    auth = mockAuth,
                    storage = mockStorage,
                    identifier = "test-identifier",
                )

            viewModel.updateCode("12345")
            viewModel.verifyOtp()

            assertTrue(viewModel.state.value is OtpVerificationState.Error)

            viewModel.clearError()
            assertTrue(viewModel.state.value is OtpVerificationState.Idle)
        }

    @Test
    fun `updateCode should clear error state`() =
        runTest {
            val mockAuth = MockAuthForOtp()
            val mockStorage = MockStorageForOtp()
            val viewModel =
                OtpVerificationViewModel(
                    auth = mockAuth,
                    storage = mockStorage,
                    identifier = "test-identifier",
                )

            viewModel.updateCode("12345")
            viewModel.verifyOtp()
            assertTrue(viewModel.state.value is OtpVerificationState.Error)

            viewModel.updateCode("123456")
            assertTrue(viewModel.state.value is OtpVerificationState.Idle)
        }
}
