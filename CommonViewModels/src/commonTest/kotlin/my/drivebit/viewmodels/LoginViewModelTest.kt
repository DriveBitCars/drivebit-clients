package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.Auth
import my.drivebit.network.services.CreateOtpResponse
import my.drivebit.network.services.VerifyOtpResponse
import my.drivebit.utils.PhoneInputValidator
import my.drivebit.utils.PhoneValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MockAuth : Auth {
    var shouldThrowError = false
    var errorMessage = "Network error"
    var lastPhoneCalled: String? = null
    var lastVerifyIdentifier: String? = null
    var lastVerifyCode: String? = null

    override suspend fun createOtp(login: String): CreateOtpResponse {
        lastPhoneCalled = login
        if (shouldThrowError) {
            throw Exception(errorMessage)
        }
        return CreateOtpResponse(
            message = "OTP sent",
            sessionId = "test-session-id-guid",
            expiresIn = 300,
        )
    }

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
                    token = "test-token",
                    expiresAt = "",
                ),
            refreshToken =
                my.drivebit.network.services.RefreshTokenDTO(
                    token = "test-refresh-token",
                    userId = "",
                    expiresAt = "",
                    createdAt = "",
                ),
        )
    }

    override suspend fun createTokens(refreshToken: String): my.drivebit.network.services.CreateNewTokensResponse {
        if (shouldThrowError) {
            throw Exception(errorMessage)
        }
        return my.drivebit.network.services.CreateNewTokensResponse(
            accessToken =
                my.drivebit.network.services.AccessTokenDTO(
                    token = "new-test-token",
                    expiresAt = "",
                ),
            refreshToken =
                my.drivebit.network.services.RefreshTokenDTO(
                    token = "new-test-refresh-token",
                    userId = "",
                    expiresAt = "",
                    createdAt = "",
                ),
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val phoneValidator = PhoneValidator()
    private val phoneInputValidator = PhoneInputValidator()

    @Test
    fun `initial state should be Idle`() =
        runTest {
            val mockAuth = MockAuth()
            val viewModel = PhoneLoginViewModel(mockAuth, phoneValidator, phoneInputValidator)

            assertTrue(viewModel.state.value is AuthFormState.Idle)
        }

    @Test
    fun `formatInput should format phone correctly`() =
        runTest {
            val mockAuth = MockAuth()
            val viewModel = PhoneLoginViewModel(mockAuth, phoneValidator, phoneInputValidator)

            val formatted = viewModel.formatInput("79991234567")
            assertEquals("+79991234567", formatted)
        }

    @Test
    fun `submit should call auth service with phone number`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockAuth = MockAuth()
            val viewModel = PhoneLoginViewModel(mockAuth, phoneValidator, phoneInputValidator, testScope)
            val testPhone = "+79991234567"

            viewModel.submit(testPhone)
            advanceUntilIdle()

            assertEquals(testPhone, mockAuth.lastPhoneCalled)
        }

    @Test
    fun `submit should set state to Success on success`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockAuth = MockAuth()
            val viewModel = PhoneLoginViewModel(mockAuth, phoneValidator, phoneInputValidator, testScope)

            viewModel.submit("+79991234567")
            advanceUntilIdle()

            assertTrue(viewModel.state.value is AuthFormState.Success)
            val successState = viewModel.state.value as AuthFormState.Success
            assertEquals("test-session-id-guid", successState.identifier)
        }

    @Test
    fun `submit should set state to Error on failure`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockAuth = MockAuth()
            mockAuth.shouldThrowError = true
            mockAuth.errorMessage = "Invalid phone number"
            val viewModel = PhoneLoginViewModel(mockAuth, phoneValidator, phoneInputValidator, testScope)

            viewModel.submit("+79991234567")
            advanceUntilIdle()

            assertTrue(viewModel.state.value is AuthFormState.Error)
            val errorState = viewModel.state.value as AuthFormState.Error
            assertEquals("Invalid phone number", errorState.message)
        }

    @Test
    fun `submit should handle exception with null message`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockAuth = MockAuth()
            mockAuth.shouldThrowError = true
            mockAuth.errorMessage = ""
            val viewModel = PhoneLoginViewModel(mockAuth, phoneValidator, phoneInputValidator, testScope)

            viewModel.submit("+79991234567")
            advanceUntilIdle()

            assertTrue(viewModel.state.value is AuthFormState.Error)
            val errorState = viewModel.state.value as AuthFormState.Error
            assertEquals("Произошла ошибка", errorState.message)
        }

    @Test
    fun `clearError should reset state to Idle`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockAuth = MockAuth()
            mockAuth.shouldThrowError = true
            val viewModel = PhoneLoginViewModel(mockAuth, phoneValidator, phoneInputValidator, testScope)

            viewModel.submit("+79991234567")
            advanceUntilIdle()
            assertTrue(viewModel.state.value is AuthFormState.Error)

            viewModel.clearError()
            assertTrue(viewModel.state.value is AuthFormState.Idle)
        }
}
