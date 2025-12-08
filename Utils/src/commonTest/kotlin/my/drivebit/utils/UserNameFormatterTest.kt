package my.drivebit.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class UserNameFormatterTest {
    @Test
    fun `formatDisplayName should return default name when all fields are null`() {
        val result =
            UserNameFormatter.formatDisplayName(
                firstName = null,
                middleName = null,
                lastName = null,
            )

        assertEquals("Имя", result)
    }

    @Test
    fun `formatDisplayName should return default name when all fields are empty`() {
        val result =
            UserNameFormatter.formatDisplayName(
                firstName = null,
                middleName = "",
                lastName = null,
            )

        assertEquals("Имя", result)
    }

    @Test
    fun `formatDisplayName should return only firstName when only firstName is provided`() {
        val result =
            UserNameFormatter.formatDisplayName(
                firstName = "Иван",
                middleName = null,
                lastName = null,
            )

        assertEquals("Иван", result)
    }

    @Test
    fun `formatDisplayName should return firstName and middleName when provided`() {
        val result =
            UserNameFormatter.formatDisplayName(
                firstName = "Иван",
                middleName = "Петрович",
                lastName = null,
            )

        assertEquals("Иван Петрович", result)
    }

    @Test
    fun `formatDisplayName should return firstName and last initial when only firstName and lastName are provided`() {
        val result =
            UserNameFormatter.formatDisplayName(
                firstName = "Иван",
                middleName = null,
                lastName = "Иванов",
            )

        assertEquals("Иван И.", result)
    }

    @Test
    fun `formatDisplayName should return full name with last initial when all fields are provided`() {
        val result =
            UserNameFormatter.formatDisplayName(
                firstName = "Иван",
                middleName = "Петрович",
                lastName = "Иванов",
            )

        assertEquals("Иван Петрович И.", result)
    }

    @Test
    fun `formatDisplayName should ignore empty middleName`() {
        val result =
            UserNameFormatter.formatDisplayName(
                firstName = "Иван",
                middleName = "",
                lastName = "Иванов",
            )

        assertEquals("Иван И.", result)
    }

    @Test
    fun `formatDisplayName should ignore empty lastName`() {
        val result =
            UserNameFormatter.formatDisplayName(
                firstName = "Иван",
                middleName = "Петрович",
                lastName = "",
            )

        assertEquals("Иван Петрович", result)
    }

    @Test
    fun `formatDisplayName should return only middleName when only middleName is provided`() {
        val result =
            UserNameFormatter.formatDisplayName(
                firstName = null,
                middleName = "Петрович",
                lastName = null,
            )

        assertEquals("Петрович", result)
    }

    @Test
    fun `formatDisplayName should return only last initial when only lastName is provided`() {
        val result =
            UserNameFormatter.formatDisplayName(
                firstName = null,
                middleName = null,
                lastName = "Иванов",
            )

        assertEquals("И.", result)
    }

    @Test
    fun `formatDisplayName should use custom default name`() {
        val result =
            UserNameFormatter.formatDisplayName(
                firstName = null,
                middleName = null,
                lastName = null,
                defaultName = "Пользователь",
            )

        assertEquals("Пользователь", result)
    }

    @Test
    fun `formatDisplayName should trim whitespace in names`() {
        val result =
            UserNameFormatter.formatDisplayName(
                firstName = "  Иван  ",
                middleName = "  Петрович  ",
                lastName = "  Иванов  ",
            )

        assertEquals("Иван Петрович И.", result)
    }
}
