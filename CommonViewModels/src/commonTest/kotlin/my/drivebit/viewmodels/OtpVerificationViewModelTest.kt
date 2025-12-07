package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MockOtpResultRepository : OtpResultRepository {
    var shouldReturnError = false
    var errorMessage = "Network error"
    var lastIdentifier: String? = null
    var lastCode: String? = null
    var lastAdditionalParams: Map<String, String>? = null

    override suspend fun otpResult(
        identifier: String,
        code: String,
        additionalParams: Map<String, String>,
    ): OtpResult {
        lastIdentifier = identifier
        lastCode = code
        lastAdditionalParams = additionalParams
        return if (shouldReturnError) {
            OtpResult.Error(errorMessage)
        } else {
            OtpResult.Success
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class OtpVerificationViewModelTest {
    @Test
    fun `initial state should be Idle`() =
        runTest {
            val mockRepository = MockOtpResultRepository()
            val viewModel =
                OtpVerificationViewModel(
                    otpResultRepository = mockRepository,
                    identifier = "test-identifier",
                )

            assertTrue(viewModel.state.value is OtpVerificationState.Idle)
            assertEquals("", viewModel.code.value)
        }

    @Test
    fun `updateCode should update code and clear error`() =
        runTest {
            val mockRepository = MockOtpResultRepository()
            val viewModel =
                OtpVerificationViewModel(
                    otpResultRepository = mockRepository,
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
            val mockRepository = MockOtpResultRepository()
            val viewModel =
                OtpVerificationViewModel(
                    otpResultRepository = mockRepository,
                    identifier = "test-identifier",
                )

            viewModel.updateCode("1234567890")
            assertEquals("123456", viewModel.code.value)
        }

    @Test
    fun `verifyOtp should set error if code length is not 6`() =
        runTest {
            val mockRepository = MockOtpResultRepository()
            val viewModel =
                OtpVerificationViewModel(
                    otpResultRepository = mockRepository,
                    identifier = "test-identifier",
                )

            viewModel.updateCode("12345")
            viewModel.verifyOtp()

            assertTrue(viewModel.state.value is OtpVerificationState.Error)
            val errorState = viewModel.state.value as OtpVerificationState.Error
            assertEquals("Введите 6-значный код", errorState.message)
        }

    @Test
    fun `verifyOtp should call repository with correct parameters`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockRepository = MockOtpResultRepository()
            val identifier = "test-session-id"
            val viewModel =
                OtpVerificationViewModel(
                    otpResultRepository = mockRepository,
                    identifier = identifier,
                    coroutineScope = testScope,
                )

            viewModel.updateCode("123456")
            viewModel.verifyOtp()
            advanceUntilIdle()

            assertEquals(identifier, mockRepository.lastIdentifier)
            assertEquals("123456", mockRepository.lastCode)
        }

    @Test
    fun `verifyOtp should set state to Success on success`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockRepository = MockOtpResultRepository()
            val viewModel =
                OtpVerificationViewModel(
                    otpResultRepository = mockRepository,
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
            val mockRepository = MockOtpResultRepository()
            mockRepository.shouldReturnError = true
            mockRepository.errorMessage = "Invalid OTP code"
            val viewModel =
                OtpVerificationViewModel(
                    otpResultRepository = mockRepository,
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
    fun `verifyOtp should handle error with empty message`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val mockRepository = MockOtpResultRepository()
            mockRepository.shouldReturnError = true
            mockRepository.errorMessage = ""
            val viewModel =
                OtpVerificationViewModel(
                    otpResultRepository = mockRepository,
                    identifier = "test-identifier",
                    coroutineScope = testScope,
                )

            viewModel.updateCode("123456")
            viewModel.verifyOtp()
            advanceUntilIdle()

            assertTrue(viewModel.state.value is OtpVerificationState.Error)
            val errorState = viewModel.state.value as OtpVerificationState.Error
            assertEquals("", errorState.message)
        }

    @Test
    fun `clearError should reset state to Idle`() =
        runTest {
            val mockRepository = MockOtpResultRepository()
            val viewModel =
                OtpVerificationViewModel(
                    otpResultRepository = mockRepository,
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
            val mockRepository = MockOtpResultRepository()
            val viewModel =
                OtpVerificationViewModel(
                    otpResultRepository = mockRepository,
                    identifier = "test-identifier",
                )

            viewModel.updateCode("12345")
            viewModel.verifyOtp()
            assertTrue(viewModel.state.value is OtpVerificationState.Error)

            viewModel.updateCode("123456")
            assertTrue(viewModel.state.value is OtpVerificationState.Idle)
        }
}
