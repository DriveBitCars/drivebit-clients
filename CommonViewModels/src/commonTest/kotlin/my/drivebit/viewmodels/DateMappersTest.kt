package my.drivebit.viewmodels

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DateMappersTest {
    @Test
    fun `mapIso8601ToDateString should format date correctly`() {
        val iso8601String = "2025-12-02T14:26:55.121463Z"
        val result = mapIso8601ToDateString(iso8601String)
        val expected =
            Instant
                .parse(iso8601String)
                .toLocalDateTime(TimeZone.currentSystemDefault())
        val expectedString = "${expected.dayOfMonth.toString().padStart(
            2,
            '0',
        )}.${expected.monthNumber.toString().padStart(2, '0')}.${expected.year}"
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
        val expectedString = "${expected.dayOfMonth.toString().padStart(
            2,
            '0',
        )}.${expected.monthNumber.toString().padStart(2, '0')}.${expected.year}"
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
        val expectedString = "${expected.dayOfMonth.toString().padStart(
            2,
            '0',
        )}.${expected.monthNumber.toString().padStart(2, '0')}.${expected.year}"
        assertEquals(expectedString, result)
    }

    @Test
    fun `mapIso8601ToDateString should return null for null input`() {
        val result = mapIso8601ToDateString(null)
        assertNull(result)
    }

    @Test
    fun `mapIso8601ToDateString should return null for invalid format`() {
        val result = mapIso8601ToDateString("invalid-date")
        assertNull(result)
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
    fun `mapIso8601ToTimeString should return null for null input`() {
        val result = mapIso8601ToTimeString(null)
        assertNull(result)
    }

    @Test
    fun `mapIso8601ToTimeString should return null for invalid format`() {
        val result = mapIso8601ToTimeString("invalid-date")
        assertNull(result)
    }
}
