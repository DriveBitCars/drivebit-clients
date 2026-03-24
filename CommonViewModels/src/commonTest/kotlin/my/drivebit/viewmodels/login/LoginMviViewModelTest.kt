package my.drivebit.viewmodels.login

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
import my.drivebit.viewmodels.AuthFormState
import my.drivebit.viewmodels.MockCreateOtpRepository
import my.drivebit.viewmodels.ValidationState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class RecordingLoginDraftStorage : LoginDraftStorage {
    var saved: LoginDraft? = null
    var cleared = false

    override fun save(draft: LoginDraft) {
        saved = draft
    }

    override fun load(): LoginDraft? = saved

    override fun clear() {
        cleared = true
        saved = null
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class LoginMviViewModelTest {
    private val phoneValidator = PhoneValidator()
    private val phoneInputValidator = PhoneInputValidator()
    private val emailValidator = EmailValidator()
    private val emailInputValidator = EmailInputValidator()

    @Test
    fun `initial auth state is Idle`() =
        runTest {
            val vm =
                LoginMviViewModelImpl(
                    kind = LoginScreenKind.Phone,
                    createOtpRepository = MockCreateOtpRepository(),
                    phoneValidator = phoneValidator,
                    phoneInputValidator = phoneInputValidator,
                    emailValidator = emailValidator,
                    emailInputValidator = emailInputValidator,
                    loginDraftStorage = RecordingLoginDraftStorage(),
                )
            assertTrue(vm.uiState.value.authState is AuthFormState.Idle)
        }

    @Test
    fun `InputChanged updates input and validation`() =
        runTest {
            val vm =
                LoginMviViewModelImpl(
                    kind = LoginScreenKind.Phone,
                    createOtpRepository = MockCreateOtpRepository(),
                    phoneValidator = phoneValidator,
                    phoneInputValidator = phoneInputValidator,
                    emailValidator = emailValidator,
                    emailInputValidator = emailInputValidator,
                    loginDraftStorage = RecordingLoginDraftStorage(),
                )
            vm.handleIntent(LoginIntent.InputChanged("79991234567"))
            assertEquals("+79991234567", vm.uiState.value.input)
            assertTrue(vm.validatorViewModel.validationState.value is ValidationState.Valid)
        }

    @Test
    fun `RestoreDraft applies stored draft`() =
        runTest {
            val storage =
                RecordingLoginDraftStorage().apply {
                    saved = LoginDraft("+79991234567", true)
                }
            val vm =
                LoginMviViewModelImpl(
                    kind = LoginScreenKind.Phone,
                    createOtpRepository = MockCreateOtpRepository(),
                    phoneValidator = phoneValidator,
                    phoneInputValidator = phoneInputValidator,
                    emailValidator = emailValidator,
                    emailInputValidator = emailInputValidator,
                    loginDraftStorage = storage,
                )
            vm.handleIntent(LoginIntent.RestoreDraft)
            assertEquals("+79991234567", vm.uiState.value.input)
            assertTrue(vm.uiState.value.termsAccepted)
        }

    @Test
    fun `RestoreDraft does not apply phone draft on email screen`() =
        runTest {
            val storage =
                RecordingLoginDraftStorage().apply {
                    saved = LoginDraft("+7", true)
                }
            val vm =
                LoginMviViewModelImpl(
                    kind = LoginScreenKind.Email,
                    createOtpRepository = MockCreateOtpRepository(),
                    phoneValidator = phoneValidator,
                    phoneInputValidator = phoneInputValidator,
                    emailValidator = emailValidator,
                    emailInputValidator = emailInputValidator,
                    loginDraftStorage = storage,
                )
            vm.handleIntent(LoginIntent.RestoreDraft)
            assertEquals("", vm.uiState.value.input)
        }

    @Test
    fun `RestoreDraft does not apply email draft on phone screen`() =
        runTest {
            val storage =
                RecordingLoginDraftStorage().apply {
                    saved = LoginDraft("user@example.com", true)
                }
            val vm =
                LoginMviViewModelImpl(
                    kind = LoginScreenKind.Phone,
                    createOtpRepository = MockCreateOtpRepository(),
                    phoneValidator = phoneValidator,
                    phoneInputValidator = phoneInputValidator,
                    emailValidator = emailValidator,
                    emailInputValidator = emailInputValidator,
                    loginDraftStorage = storage,
                )
            vm.handleIntent(LoginIntent.RestoreDraft)
            assertEquals("+7", vm.uiState.value.input)
        }

    @Test
    fun `Submit reaches Success when otp succeeds`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val repo = MockCreateOtpRepository()
            val vm =
                LoginMviViewModelImpl(
                    kind = LoginScreenKind.Phone,
                    createOtpRepository = repo,
                    phoneValidator = phoneValidator,
                    phoneInputValidator = phoneInputValidator,
                    emailValidator = emailValidator,
                    emailInputValidator = emailInputValidator,
                    loginDraftStorage = RecordingLoginDraftStorage(),
                    coroutineScope = testScope,
                )
            vm.handleIntent(LoginIntent.InputChanged("79991234567"))
            vm.handleIntent(LoginIntent.Submit)
            advanceUntilIdle()
            assertTrue(vm.uiState.value.authState is AuthFormState.Success)
            val success = vm.uiState.value.authState as AuthFormState.Success
            assertEquals("test-session-id-guid", success.identifier)
        }

    @Test
    fun `clearDraftStorage clears storage`() =
        runTest {
            val storage = RecordingLoginDraftStorage()
            val vm =
                LoginMviViewModelImpl(
                    kind = LoginScreenKind.Phone,
                    createOtpRepository = MockCreateOtpRepository(),
                    phoneValidator = phoneValidator,
                    phoneInputValidator = phoneInputValidator,
                    emailValidator = emailValidator,
                    emailInputValidator = emailInputValidator,
                    loginDraftStorage = storage,
                )
            vm.clearDraftStorage()
            assertTrue(storage.cleared)
        }

    @Test
    fun `TermsChanged updates state when consent required`() =
        runTest {
            val vm =
                LoginMviViewModelImpl(
                    kind = LoginScreenKind.Phone,
                    createOtpRepository = MockCreateOtpRepository(),
                    phoneValidator = phoneValidator,
                    phoneInputValidator = phoneInputValidator,
                    emailValidator = emailValidator,
                    emailInputValidator = emailInputValidator,
                    loginDraftStorage = RecordingLoginDraftStorage(),
                    requireTermsConsent = true,
                )
            assertEquals(false, vm.uiState.value.termsAccepted)
            vm.handleIntent(LoginIntent.TermsChanged(true))
            assertEquals(true, vm.uiState.value.termsAccepted)
        }
}
