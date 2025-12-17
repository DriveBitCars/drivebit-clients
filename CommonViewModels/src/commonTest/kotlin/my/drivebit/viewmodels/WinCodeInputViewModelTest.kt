package my.drivebit.viewmodels

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import my.drivebit.utils.InputValidator
import my.drivebit.utils.ValidationResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeWinCodeValidator : InputValidator {
    var shouldReturnValid = false
    var errorMessage: String = "Введите WIN код"

    override fun isValid(input: String): ValidationResult {
        return if (shouldReturnValid) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errorMessage)
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class WinCodeInputViewModelTest {
    @Test
    fun `initial state should have empty win code and error validation`() =
        runTest {
            val validator = FakeWinCodeValidator()
            val viewModel = WinCodeInputViewModel(validator)

            assertEquals("", viewModel.winCode.value)
            assertTrue(viewModel.validationState.value is ValidationState.Error)
            assertFalse(viewModel.isValid)
        }

    @Test
    fun `updateWinCode should update win code value and convert to uppercase`() =
        runTest {
            val validator = FakeWinCodeValidator()
            val viewModel = WinCodeInputViewModel(validator)

            viewModel.updateWinCode("1hgbh41jxmn109186")

            assertEquals("1HGBH41JXMN109186", viewModel.winCode.value)
        }

    @Test
    fun `updateWinCode should validate win code and set Valid state when valid`() =
        runTest {
            val validator = FakeWinCodeValidator().apply { shouldReturnValid = true }
            val viewModel = WinCodeInputViewModel(validator)

            viewModel.updateWinCode("1HGBH41JXMN109186")

            assertTrue(viewModel.validationState.value is ValidationState.Valid)
            assertTrue(viewModel.isValid)
        }

    @Test
    fun `updateWinCode should validate win code and set Error state when invalid`() =
        runTest {
            val validator =
                FakeWinCodeValidator().apply {
                    shouldReturnValid = false
                    errorMessage = "WIN код должен содержать 17 символов"
                }
            val viewModel = WinCodeInputViewModel(validator)

            viewModel.updateWinCode("123")

            assertTrue(viewModel.validationState.value is ValidationState.Error)
            val errorState = viewModel.validationState.value as ValidationState.Error
            assertEquals("WIN код должен содержать 17 символов", errorState.message)
            assertFalse(viewModel.isValid)
        }

    @Test
    fun `isValid should return true when validation state is Valid`() =
        runTest {
            val validator = FakeWinCodeValidator().apply { shouldReturnValid = true }
            val viewModel = WinCodeInputViewModel(validator)

            viewModel.updateWinCode("1HGBH41JXMN109186")

            assertTrue(viewModel.isValid)
        }

    @Test
    fun `isValid should return false when validation state is Error`() =
        runTest {
            val validator = FakeWinCodeValidator().apply { shouldReturnValid = false }
            val viewModel = WinCodeInputViewModel(validator)

            viewModel.updateWinCode("123")

            assertFalse(viewModel.isValid)
        }

    @Test
    fun `clearError should revalidate win code`() =
        runTest {
            val validator = FakeWinCodeValidator().apply { shouldReturnValid = false }
            val viewModel = WinCodeInputViewModel(validator)

            viewModel.updateWinCode("123")
            assertFalse(viewModel.isValid)

            validator.shouldReturnValid = true
            viewModel.clearError()

            assertTrue(viewModel.isValid)
        }
}

