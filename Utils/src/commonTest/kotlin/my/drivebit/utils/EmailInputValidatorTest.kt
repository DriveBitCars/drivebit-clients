package my.drivebit.utils

import kotlin.test.Test
import kotlin.test.assertTrue

class EmailInputValidatorTest {
    private val validator = EmailInputValidator()

    @Test
    fun `isValid should return Invalid for empty string`() {
        val result = validator.isValid("")
        assertTrue(result is ValidationResult.Invalid)
        assertTrue((result as ValidationResult.Invalid).errorMessage.isNotEmpty())
    }

    @Test
    fun `isValid should return Invalid for string without at symbol`() {
        val result = validator.isValid("invalidemail")
        assertTrue(result is ValidationResult.Invalid)
        assertTrue((result as ValidationResult.Invalid).errorMessage.isNotEmpty())
    }

    @Test
    fun `isValid should return Invalid for email without domain`() {
        val result = validator.isValid("user@")
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `isValid should return Invalid for email without TLD`() {
        val result = validator.isValid("user@domain")
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `isValid should return Invalid for email with spaces`() {
        val result = validator.isValid("user @domain.com")
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `isValid should return Valid for valid email`() {
        val result = validator.isValid("test@example.com")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `isValid should return Valid for email with subdomain`() {
        val result = validator.isValid("user@mail.example.com")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `isValid should return Valid for email with plus sign`() {
        val result = validator.isValid("user+tag@example.com")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `isValid should return Valid for email with dots`() {
        val result = validator.isValid("first.last@example.com")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `isValid should return Valid for email with numbers`() {
        val result = validator.isValid("user123@example.com")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `isValid should return Valid for email with uppercase`() {
        val result = validator.isValid("USER@EXAMPLE.COM")
        assertTrue(result is ValidationResult.Valid)
    }
}
