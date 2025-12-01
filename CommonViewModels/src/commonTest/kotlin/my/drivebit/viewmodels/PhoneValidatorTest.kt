package my.drivebit.viewmodels

import kotlin.test.Test
import kotlin.test.assertEquals

class PhoneValidatorTest {
    private val validator = PhoneValidator()

    @Test
    fun `validate empty string should return +7`() {
        val result = validator.validate("")
        assertEquals("+7", result)
    }

    @Test
    fun `validate string with only non-digits should return +7`() {
        val result = validator.validate("abc")
        assertEquals("+7", result)
    }

    @Test
    fun `validate phone starting with 7 should format correctly`() {
        val result = validator.validate("79991234567")
        assertEquals("+79991234567", result)
    }

    @Test
    fun `validate phone starting with 8 should format correctly`() {
        val result = validator.validate("89991234567")
        assertEquals("+79991234567", result)
    }

    @Test
    fun `validate phone without country code should format correctly`() {
        val result = validator.validate("9991234567")
        assertEquals("+79991234567", result)
    }

    @Test
    fun `validate phone with plus sign should format correctly`() {
        val result = validator.validate("+79991234567")
        assertEquals("+79991234567", result)
    }

    @Test
    fun `validate phone with spaces should format correctly`() {
        val result = validator.validate("7 999 123 45 67")
        assertEquals("+79991234567", result)
    }

    @Test
    fun `validate phone with dashes should format correctly`() {
        val result = validator.validate("7-999-123-45-67")
        assertEquals("+79991234567", result)
    }

    @Test
    fun `validate phone with parentheses should format correctly`() {
        val result = validator.validate("7(999)123-45-67")
        assertEquals("+79991234567", result)
    }

    @Test
    fun `validate phone with more than 10 digits should take only 10`() {
        val result = validator.validate("7999123456789")
        assertEquals("+79991234567", result)
    }

    @Test
    fun `validate phone with less than 10 digits should format correctly`() {
        val result = validator.validate("9991234")
        assertEquals("+79991234", result)
    }
}
