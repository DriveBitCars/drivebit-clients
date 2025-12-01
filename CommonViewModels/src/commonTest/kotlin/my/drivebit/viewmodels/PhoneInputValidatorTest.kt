package my.drivebit.viewmodels

import kotlin.test.Test
import kotlin.test.assertTrue

class PhoneInputValidatorTest {
    private val validator = PhoneInputValidator()

    @Test
    fun `isValid should return Invalid for empty string`() {
        val result = validator.isValid("")
        assertTrue(result is ValidationResult.Invalid)
        assertTrue((result as ValidationResult.Invalid).errorMessage.contains("Введите номер телефона"))
    }

    @Test
    fun `isValid should return Invalid for string with only non-digits`() {
        val result = validator.isValid("abc")
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `isValid should return Invalid for phone with less than 10 digits`() {
        val result = validator.isValid("79991234")
        assertTrue(result is ValidationResult.Invalid)
        assertTrue((result as ValidationResult.Invalid).errorMessage.contains("10 цифр"))
    }

    @Test
    fun `isValid should return Invalid for phone with more than 10 digits`() {
        val result = validator.isValid("799912345678")
        assertTrue(result is ValidationResult.Invalid)
        assertTrue((result as ValidationResult.Invalid).errorMessage.contains("10 цифр"))
    }

    @Test
    fun `isValid should return Invalid for phone with letters`() {
        val result = validator.isValid("7999abc1234")
        assertTrue(result is ValidationResult.Invalid)
        assertTrue((result as ValidationResult.Invalid).errorMessage.contains("только цифры"))
    }

    @Test
    fun `isValid should return Valid for phone starting with 7 and 10 digits`() {
        val result = validator.isValid("79991234567")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `isValid should return Valid for phone starting with 8 and 10 digits`() {
        val result = validator.isValid("89991234567")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `isValid should return Valid for phone without country code and 10 digits`() {
        val result = validator.isValid("9991234567")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `isValid should return Valid for phone with formatting characters`() {
        val result = validator.isValid("7 (999) 123-45-67")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `isValid should return Valid for phone with plus sign`() {
        val result = validator.isValid("+79991234567")
        assertTrue(result is ValidationResult.Valid)
    }
}
