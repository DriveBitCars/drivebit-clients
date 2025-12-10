package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.utils.PhoneInputValidator
import my.drivebit.utils.PhoneValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val phoneValidator = PhoneValidator()
    private val phoneInputValidator = PhoneInputValidator()

    @Test
    fun `initial state should be Idle`() =
        runTest {
            val repo = MockCreateOtpRepository()
            val viewModel = PhoneLoginViewModel(repo, phoneValidator, phoneInputValidator)

            assertTrue(viewModel.state.value is AuthFormState.Idle)
        }

    @Test
    fun `formatInput should format phone correctly`() =
        runTest {
            val repo = MockCreateOtpRepository()
            val viewModel = PhoneLoginViewModel(repo, phoneValidator, phoneInputValidator)

            val formatted = viewModel.formatInput("79991234567")
            assertEquals("+79991234567", formatted)
        }

    @Test
    fun `submit should call auth service with phone number`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val repo = MockCreateOtpRepository()
            val viewModel = PhoneLoginViewModel(repo, phoneValidator, phoneInputValidator, testScope)
            val testPhone = "+79991234567"

            viewModel.submit(testPhone)
            advanceUntilIdle()

            assertEquals(testPhone, repo.lastLoginCalled)
        }

    @Test
    fun `submit should set state to Success on success`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val repo = MockCreateOtpRepository()
            val viewModel = PhoneLoginViewModel(repo, phoneValidator, phoneInputValidator, testScope)

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
            val repo = MockCreateOtpRepository()
            repo.shouldThrowError = true
            repo.errorMessage = "Invalid phone number"
            val viewModel = PhoneLoginViewModel(repo, phoneValidator, phoneInputValidator, testScope)

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
            val repo = MockCreateOtpRepository()
            repo.shouldThrowError = true
            repo.errorMessage = ""
            val viewModel = PhoneLoginViewModel(repo, phoneValidator, phoneInputValidator, testScope)

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
            val repo = MockCreateOtpRepository()
            repo.shouldThrowError = true
            val viewModel = PhoneLoginViewModel(repo, phoneValidator, phoneInputValidator, testScope)

            viewModel.submit("+79991234567")
            advanceUntilIdle()
            assertTrue(viewModel.state.value is AuthFormState.Error)

            viewModel.clearError()
            assertTrue(viewModel.state.value is AuthFormState.Idle)
        }
}
