package my.drivebit.viewmodels

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import my.drivebit.utils.InputValidator
import my.drivebit.utils.ValidationResult
import my.drivebit.utils.Validator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeValidator : Validator {
    override fun validate(input: String): String = input.trim()
}

private class FakeInputValidator : InputValidator {
    var shouldReturnValid = true
    var errorMessage = "Invalid input"

    override fun isValid(input: String): ValidationResult =
        if (shouldReturnValid) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errorMessage)
        }
}

@OptIn(ExperimentalCoroutinesApi::class)
class InputFieldViewModelTest {
    @Test
    fun `initial state should be Error with provided message`() =
        runTest {
            val viewModel =
                ValidatorViewModel(
                    FakeValidator(),
                    FakeInputValidator(),
                    initialErrorMessage = "Initial error",
                )

            assertFalse(viewModel.isValid)
            assertTrue(viewModel.validationState.value is ValidationState.Error)
            val errorState = viewModel.validationState.value as ValidationState.Error
            assertEquals("Initial error", errorState.message)
        }

    @Test
    fun `validateInput should set Valid state when input is valid`() =
        runTest {
            val validator = FakeInputValidator().apply { shouldReturnValid = true }
            val viewModel = ValidatorViewModel(FakeValidator(), validator)

            viewModel.validateInput("valid@example.com")

            assertTrue(viewModel.isValid)
            assertTrue(viewModel.validationState.value is ValidationState.Valid)
        }

    @Test
    fun `validateInput should set Error state when input is invalid`() =
        runTest {
            val validator =
                FakeInputValidator().apply {
                    shouldReturnValid = false
                    errorMessage = "Invalid email"
                }
            val viewModel = ValidatorViewModel(FakeValidator(), validator)

            viewModel.validateInput("invalid")

            assertFalse(viewModel.isValid)
            assertTrue(viewModel.validationState.value is ValidationState.Error)
            val errorState = viewModel.validationState.value as ValidationState.Error
            assertEquals("Invalid email", errorState.message)
        }

    @Test
    fun `setError should set Error state with provided message`() =
        runTest {
            val viewModel = ValidatorViewModel(FakeValidator(), FakeInputValidator())
            viewModel.validateInput("valid@example.com")
            assertTrue(viewModel.isValid)

            viewModel.setError("Server error occurred")

            assertFalse(viewModel.isValid)
            assertTrue(viewModel.validationState.value is ValidationState.Error)
            val errorState = viewModel.validationState.value as ValidationState.Error
            assertEquals("Server error occurred", errorState.message)
        }

    @Test
    fun `clearError should set Valid state`() =
        runTest {
            val validator =
                FakeInputValidator().apply {
                    shouldReturnValid = false
                    errorMessage = "Error"
                }
            val viewModel = ValidatorViewModel(FakeValidator(), validator)
            viewModel.validateInput("invalid")
            assertFalse(viewModel.isValid)

            viewModel.clearError()

            assertTrue(viewModel.isValid)
            assertTrue(viewModel.validationState.value is ValidationState.Valid)
        }

    @Test
    fun `formatInput should use validator to format input`() =
        runTest {
            val viewModel = ValidatorViewModel(FakeValidator(), FakeInputValidator())

            val formatted = viewModel.formatInput("  test@example.com  ")

            assertEquals("test@example.com", formatted)
        }

    @Test
    fun `isValid should return true when validation state is Valid`() =
        runTest {
            val validator = FakeInputValidator().apply { shouldReturnValid = true }
            val viewModel = ValidatorViewModel(FakeValidator(), validator)

            viewModel.validateInput("valid@example.com")

            assertTrue(viewModel.isValid)
        }

    @Test
    fun `isValid should return false when validation state is Error`() =
        runTest {
            val validator = FakeInputValidator().apply { shouldReturnValid = false }
            val viewModel = ValidatorViewModel(FakeValidator(), validator)

            viewModel.validateInput("invalid")

            assertFalse(viewModel.isValid)
        }
}
