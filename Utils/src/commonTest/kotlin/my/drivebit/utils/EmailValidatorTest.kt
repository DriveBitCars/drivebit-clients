package my.drivebit.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmailValidatorTest {
    private val validator = EmailValidator()

    @Test
    fun `validate empty string should return empty`() {
        val result = validator.validate("")
        assertEquals("", result)
    }

    @Test
    fun `validate valid email should return lowercase`() {
        val result = validator.validate("Test@Example.COM")
        assertEquals("test@example.com", result)
    }

    @Test
    fun `validate email with spaces should trim`() {
        val result = validator.validate("  test@example.com  ")
        assertEquals("test@example.com", result)
    }

    @Test
    fun `validate email with uppercase should return lowercase`() {
        val result = validator.validate("USER@DOMAIN.COM")
        assertEquals("user@domain.com", result)
    }

    @Test
    fun `validate email with mixed case should return lowercase`() {
        val result = validator.validate("Test.User@Example.COM")
        assertEquals("test.user@example.com", result)
    }

    @Test
    fun `validate email with plus sign should preserve it`() {
        val result = validator.validate("user+tag@example.com")
        assertEquals("user+tag@example.com", result)
    }

    @Test
    fun `validate email with dots should preserve them`() {
        val result = validator.validate("first.last@example.com")
        assertEquals("first.last@example.com", result)
    }

    @Test
    fun `validate email with numbers should preserve them`() {
        val result = validator.validate("user123@example.com")
        assertEquals("user123@example.com", result)
    }

    @Test
    fun `validate email with dash in domain should preserve it`() {
        val result = validator.validate("user@example-domain.com")
        assertEquals("user@example-domain.com", result)
    }

    @Test
    fun `validate email with subdomain should preserve it`() {
        val result = validator.validate("user@mail.example.com")
        assertEquals("user@mail.example.com", result)
    }

    @Test
    fun `isValidEmail should return true for valid email`() {
        assertTrue(validator.isValidEmail("test@example.com"))
        assertTrue(validator.isValidEmail("user.name@domain.co.uk"))
        assertTrue(validator.isValidEmail("first+last@example.org"))
    }

    @Test
    fun `isValidEmail should return false for invalid email`() {
        assertFalse(validator.isValidEmail("invalid"))
        assertFalse(validator.isValidEmail("@example.com"))
        assertFalse(validator.isValidEmail("user@"))
        assertFalse(validator.isValidEmail("user@domain"))
        assertFalse(validator.isValidEmail("user @domain.com"))
        assertFalse(validator.isValidEmail("user@domain .com"))
    }

    @Test
    fun `validate invalid email should still return normalized string`() {
        val result = validator.validate("INVALID@")
        assertEquals("invalid@", result)
    }
}
