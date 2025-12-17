package my.drivebit.utils

import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class WinCodeValidatorTest {
    private val validator = WinCodeValidator()

    @Test
    fun `isValid should return Valid for correct 17 character WIN code`() {
        val result = validator.isValid("1HGBH41JXMN109186")

        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `isValid should return Valid for WIN code with letters and digits`() {
        val result = validator.isValid("ABCD1234EFGH56789")

        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `isValid should return Invalid for empty string`() {
        val result = validator.isValid("")

        assertIs<ValidationResult.Invalid>(result)
        assertTrue((result as ValidationResult.Invalid).errorMessage.contains("Введите WIN код"))
    }

    @Test
    fun `isValid should return Invalid for whitespace only`() {
        val result = validator.isValid("   ")

        assertIs<ValidationResult.Invalid>(result)
        assertTrue((result as ValidationResult.Invalid).errorMessage.contains("Введите WIN код"))
    }

    @Test
    fun `isValid should return Invalid for code shorter than 17 characters`() {
        val result = validator.isValid("1HGBH41JXMN10918")

        assertIs<ValidationResult.Invalid>(result)
        assertTrue((result as ValidationResult.Invalid).errorMessage.contains("17 символов"))
    }

    @Test
    fun `isValid should return Invalid for code longer than 17 characters`() {
        val result = validator.isValid("1HGBH41JXMN1091867")

        assertIs<ValidationResult.Invalid>(result)
        assertTrue((result as ValidationResult.Invalid).errorMessage.contains("17 символов"))
    }

    @Test
    fun `isValid should return Invalid for code containing letter I`() {
        val result = validator.isValid("1HGBH41JXMN109I86")

        assertIs<ValidationResult.Invalid>(result)
        assertTrue((result as ValidationResult.Invalid).errorMessage.contains("недопустимые символы"))
    }

    @Test
    fun `isValid should return Invalid for code containing letter O`() {
        val result = validator.isValid("1HGBH41JXMN109O86")

        assertIs<ValidationResult.Invalid>(result)
        assertTrue((result as ValidationResult.Invalid).errorMessage.contains("недопустимые символы"))
    }

    @Test
    fun `isValid should return Invalid for code containing letter Q`() {
        val result = validator.isValid("1HGBH41JXMN109Q86")

        assertIs<ValidationResult.Invalid>(result)
        assertTrue((result as ValidationResult.Invalid).errorMessage.contains("недопустимые символы"))
    }

    @Test
    fun `isValid should return Invalid for code containing special characters`() {
        val result = validator.isValid("1HGBH41JXMN-09186")

        assertIs<ValidationResult.Invalid>(result)
        assertTrue((result as ValidationResult.Invalid).errorMessage.contains("недопустимые символы"))
    }

    @Test
    fun `isValid should convert lowercase to uppercase`() {
        val result = validator.isValid("1hgbh41jxmn109186")

        assertIs<ValidationResult.Valid>(result)
    }

    @Test
    fun `isValid should trim whitespace`() {
        val result = validator.isValid("  1HGBH41JXMN109186  ")

        assertIs<ValidationResult.Valid>(result)
    }
}

