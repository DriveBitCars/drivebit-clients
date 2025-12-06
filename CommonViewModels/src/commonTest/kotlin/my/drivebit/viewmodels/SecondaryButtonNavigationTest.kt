package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.utils.EmailInputValidator
import my.drivebit.utils.EmailValidator
import my.drivebit.utils.PhoneInputValidator
import my.drivebit.utils.PhoneValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SecondaryButtonNavigationTest {
    private val phoneValidator = PhoneValidator()
    private val phoneInputValidator = PhoneInputValidator()
    private val emailValidator = EmailValidator()
    private val emailInputValidator = EmailInputValidator()

    @Test
    fun `secondaryButtonNavigationPath should be accessible even when primary button has validation error`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val repo = MockCreateOtpRepository()
            val phoneViewModel = PhoneLoginViewModel(repo, phoneValidator, phoneInputValidator, testScope)

            phoneViewModel.validateInput("799")
            advanceUntilIdle()

            val validationState = phoneViewModel.validationState.value
            assertTrue(validationState is ValidationState.Error, "Validation should be in error state")

            val navigationPath = phoneViewModel.secondaryButtonNavigationPath
            assertEquals("/login-by-mail", navigationPath, "Secondary button navigation path should be accessible")
        }

    @Test
    fun `secondaryButtonNavigationPath should be accessible even when primary button has API error`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val repo = MockCreateOtpRepository()
            repo.shouldThrowError = true
            repo.errorMessage = "Network error"
            val phoneViewModel = PhoneLoginViewModel(repo, phoneValidator, phoneInputValidator, testScope)

            phoneViewModel.submit("+79991234567")
            advanceUntilIdle()

            val loginState = phoneViewModel.state.value
            assertTrue(loginState is AuthFormState.Error, "Login state should be in error state")

            val navigationPath = phoneViewModel.secondaryButtonNavigationPath
            assertEquals(
                "/login-by-mail",
                navigationPath,
                "Secondary button navigation path should be accessible even after API error",
            )
        }

    @Test
    fun `email viewModel secondary path should be accessible on validation error`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val repo = MockCreateOtpRepository()
            val emailViewModel =
                EmailLoginViewModel(repo, emailValidator, emailInputValidator, testScope)

            emailViewModel.validateInput("test@")
            advanceUntilIdle()

            val validationState = emailViewModel.validationState.value
            assertTrue(validationState is ValidationState.Error, "Validation should be in error state")

            val navigationPath = emailViewModel.secondaryButtonNavigationPath
            assertEquals("/login-by-phone", navigationPath, "Secondary button navigation path should be accessible")
        }

    @Test
    fun `email viewModel secondaryButtonNavigationPath should be accessible even when primary button has API error`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val repo = MockCreateOtpRepository()
            repo.shouldThrowError = true
            repo.errorMessage = "Network error"
            val emailViewModel =
                EmailLoginViewModel(repo, emailValidator, emailInputValidator, testScope)

            emailViewModel.submit("test@example.com")
            advanceUntilIdle()

            val loginState = emailViewModel.state.value
            assertTrue(loginState is AuthFormState.Error, "Login state should be in error state")

            val navigationPath = emailViewModel.secondaryButtonNavigationPath
            assertEquals(
                "/login-by-phone",
                navigationPath,
                "Secondary button navigation path should be accessible even after API error",
            )
        }

    @Test
    fun `secondaryButtonNavigationPath should be accessible during loading state`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val repo = MockCreateOtpRepository()
            val phoneViewModel = PhoneLoginViewModel(repo, phoneValidator, phoneInputValidator, testScope)

            phoneViewModel.submit("+79991234567")

            val navigationPath = phoneViewModel.secondaryButtonNavigationPath
            assertEquals(
                "/login-by-mail",
                navigationPath,
                "Secondary button navigation path should be accessible immediately after submit",
            )

            advanceUntilIdle()

            val loginStateAfter = phoneViewModel.state.value
            assertTrue(
                loginStateAfter is AuthFormState.Success,
                "Login state should be in success state after completion",
            )

            val navigationPathAfter = phoneViewModel.secondaryButtonNavigationPath
            assertEquals(
                "/login-by-mail",
                navigationPathAfter,
                "Secondary button navigation path should be accessible after loading completes",
            )
        }
}
