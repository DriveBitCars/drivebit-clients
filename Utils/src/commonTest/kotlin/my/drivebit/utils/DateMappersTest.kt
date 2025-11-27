package my.drivebit.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DateMappersTest {
    @Test
    fun `mapIso8601ToDateString should format date correctly`() {
        val iso8601String = "2025-12-02T14:26:55.121463Z"
        val result = mapIso8601ToDateString(iso8601String)
        val expected =
            Instant
                .parse(iso8601String)
                .toLocalDateTime(TimeZone.currentSystemDefault())
        val expectedString = "${expected.day.toString().padStart(
            2,
            '0',
        )}.${expected.month.number.toString().padStart(2, '0')}.${expected.year}"
        assertEquals(expectedString, result)
    }

    @Test
    fun `mapIso8601ToDateString should format date with milliseconds correctly`() {
        val iso8601String = "2025-12-03T12:00:00.000Z"
        val result = mapIso8601ToDateString(iso8601String)
        val expected =
            Instant
                .parse(iso8601String)
                .toLocalDateTime(TimeZone.currentSystemDefault())
        val expectedString = "${expected.day.toString().padStart(
            2,
            '0',
        )}.${expected.month.number.toString().padStart(2, '0')}.${expected.year}"
        assertEquals(expectedString, result)
    }

    @Test
    fun `mapIso8601ToDateString should format date for 21st month 2033`() {
        val iso8601String = "2033-09-21T10:00:00.000Z"
        val result = mapIso8601ToDateString(iso8601String)
        val expected =
            Instant
                .parse(iso8601String)
                .toLocalDateTime(TimeZone.currentSystemDefault())
        val expectedString = "${expected.day.toString().padStart(
            2,
            '0',
        )}.${expected.month.number.toString().padStart(2, '0')}.${expected.year}"
        assertEquals(expectedString, result)
    }

    @Test
    fun `mapIso8601ToDateString should throw exception for invalid format`() {
        assertFailsWith<IllegalArgumentException> {
            mapIso8601ToDateString("invalid-date")
        }
    }

    @Test
    fun `mapIso8601ToTimeString should format time correctly using system timezone`() {
        val iso8601String = "2025-12-02T14:26:55.121463Z"
        val result = mapIso8601ToTimeString(iso8601String)
        val expected =
            Instant
                .parse(iso8601String)
                .toLocalDateTime(TimeZone.currentSystemDefault())
        val expectedString = "${expected.hour.toString().padStart(
            2,
            '0',
        )}:${expected.minute.toString().padStart(2, '0')}"
        assertEquals(expectedString, result)
    }

    @Test
    fun `mapIso8601ToTimeString should format time with milliseconds correctly`() {
        val iso8601String = "2025-12-03T12:00:00.000Z"
        val result = mapIso8601ToTimeString(iso8601String)
        val expected =
            Instant
                .parse(iso8601String)
                .toLocalDateTime(TimeZone.currentSystemDefault())
        val expectedString = "${expected.hour.toString().padStart(
            2,
            '0',
        )}:${expected.minute.toString().padStart(2, '0')}"
        assertEquals(expectedString, result)
    }

    @Test
    fun `mapIso8601ToTimeString should format time for 10 00 UTC`() {
        val iso8601String = "2033-09-21T10:00:00.000Z"
        val result = mapIso8601ToTimeString(iso8601String)
        val expected =
            Instant
                .parse(iso8601String)
                .toLocalDateTime(TimeZone.currentSystemDefault())
        val expectedString = "${expected.hour.toString().padStart(
            2,
            '0',
        )}:${expected.minute.toString().padStart(2, '0')}"
        assertEquals(expectedString, result)
    }

    @Test
    fun `mapIso8601ToTimeString should throw exception for invalid format`() {
        assertFailsWith<IllegalArgumentException> {
            mapIso8601ToTimeString("invalid-date")
        }
    }

    @Test
    fun `mapIso8601ToMonthYearString should format date correctly`() {
        val iso8601String = "2025-12-02T14:26:55.121463Z"
        val result = mapIso8601ToMonthYearString(iso8601String)
        assertEquals("декабрь 2025", result)
    }

    @Test
    fun `mapIso8601ToMonthYearString should format date for January`() {
        val iso8601String = "2023-01-15T10:30:00Z"
        val result = mapIso8601ToMonthYearString(iso8601String)
        assertEquals("январь 2023", result)
    }

    @Test
    fun `mapIso8601ToMonthYearString should format date for September`() {
        val iso8601String = "2023-09-21T10:00:00.000Z"
        val result = mapIso8601ToMonthYearString(iso8601String)
        assertEquals("сентябрь 2023", result)
    }

    @Test
    fun `mapIso8601ToMonthYearString should throw exception for invalid format`() {
        assertFailsWith<IllegalArgumentException> {
            mapIso8601ToMonthYearString("invalid-date")
        }
    }
}
