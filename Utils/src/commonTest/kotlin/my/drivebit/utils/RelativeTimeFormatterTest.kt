package my.drivebit.utils

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class RelativeTimeFormatterTest {
    @Test
    fun `formatRelativeTime returns только что for 30 seconds ago`() {
        val now = Instant.parse("2025-06-17T12:00:00Z")
        val past = Instant.parse("2025-06-17T11:59:30Z")
        assertEquals("только что", formatRelativeTime(past, now))
    }

    @Test
    fun `formatRelativeTime returns 1 минуту назад`() {
        val now = Instant.parse("2025-06-17T12:00:00Z")
        val past = Instant.parse("2025-06-17T11:59:00Z")
        assertEquals("1 минуту назад", formatRelativeTime(past, now))
    }

    @Test
    fun `formatRelativeTime returns 5 минут назад`() {
        val now = Instant.parse("2025-06-17T12:00:00Z")
        val past = Instant.parse("2025-06-17T11:55:00Z")
        assertEquals("5 минут назад", formatRelativeTime(past, now))
    }

    @Test
    fun `formatRelativeTime returns 2 минуты назад`() {
        val now = Instant.parse("2025-06-17T12:00:00Z")
        val past = Instant.parse("2025-06-17T11:58:00Z")
        assertEquals("2 минуты назад", formatRelativeTime(past, now))
    }

    @Test
    fun `formatRelativeTime returns 1 час назад`() {
        val now = Instant.parse("2025-06-17T12:00:00Z")
        val past = Instant.parse("2025-06-17T11:00:00Z")
        assertEquals("1 час назад", formatRelativeTime(past, now))
    }

    @Test
    fun `formatRelativeTime returns 3 часа назад`() {
        val now = Instant.parse("2025-06-17T12:00:00Z")
        val past = Instant.parse("2025-06-17T09:00:00Z")
        assertEquals("3 часа назад", formatRelativeTime(past, now))
    }

    @Test
    fun `formatRelativeTime returns 2 часа назад`() {
        val now = Instant.parse("2025-06-17T12:00:00Z")
        val past = Instant.parse("2025-06-17T10:00:00Z")
        assertEquals("2 часа назад", formatRelativeTime(past, now))
    }

    @Test
    fun `formatRelativeTime returns 5 часов назад`() {
        val now = Instant.parse("2025-06-17T12:00:00Z")
        val past = Instant.parse("2025-06-17T07:00:00Z")
        assertEquals("5 часов назад", formatRelativeTime(past, now))
    }

    @Test
    fun `formatRelativeTime returns 1 день назад`() {
        val now = Instant.parse("2025-06-17T12:00:00Z")
        val past = Instant.parse("2025-06-16T12:00:00Z")
        assertEquals("1 день назад", formatRelativeTime(past, now))
    }

    @Test
    fun `formatRelativeTime returns 3 дня назад`() {
        val now = Instant.parse("2025-06-17T12:00:00Z")
        val past = Instant.parse("2025-06-14T12:00:00Z")
        assertEquals("3 дня назад", formatRelativeTime(past, now))
    }

    @Test
    fun `formatRelativeTime returns 5 дней назад`() {
        val now = Instant.parse("2025-06-17T12:00:00Z")
        val past = Instant.parse("2025-06-12T12:00:00Z")
        assertEquals("5 дней назад", formatRelativeTime(past, now))
    }

    @Test
    fun `formatRelativeTime returns 1 неделю назад`() {
        val now = Instant.parse("2025-06-17T12:00:00Z")
        val past = Instant.parse("2025-06-10T12:00:00Z")
        assertEquals("1 неделю назад", formatRelativeTime(past, now))
    }

    @Test
    fun `formatRelativeTime returns 2 недели назад`() {
        val now = Instant.parse("2025-06-17T12:00:00Z")
        val past = Instant.parse("2025-06-03T12:00:00Z")
        assertEquals("2 недели назад", formatRelativeTime(past, now))
    }

    @Test
    fun `formatRelativeTime returns 3 недели назад`() {
        val now = Instant.parse("2025-06-17T12:00:00Z")
        val past = Instant.parse("2025-05-27T12:00:00Z")
        assertEquals("3 недели назад", formatRelativeTime(past, now))
    }

    @Test
    fun `formatRelativeTime returns 1 месяц назад`() {
        val now = Instant.parse("2025-06-17T12:00:00Z")
        val past = Instant.parse("2025-05-17T12:00:00Z")
        assertEquals("1 месяц назад", formatRelativeTime(past, now))
    }

    @Test
    fun `formatRelativeTime returns 4 месяца назад`() {
        val now = Instant.parse("2025-06-17T12:00:00Z")
        val past = Instant.parse("2025-02-17T12:00:00Z")
        assertEquals("4 месяца назад", formatRelativeTime(past, now))
    }

    @Test
    fun `formatRelativeTime returns 11 месяцев назад`() {
        val now = Instant.parse("2025-06-17T12:00:00Z")
        val past = Instant.parse("2024-07-17T12:00:00Z")
        assertEquals("11 месяцев назад", formatRelativeTime(past, now))
    }

    @Test
    fun `formatRelativeTime returns 2 месяца назад`() {
        val now = Instant.parse("2025-06-17T12:00:00Z")
        val past = Instant.parse("2025-04-17T12:00:00Z")
        assertEquals("2 месяца назад", formatRelativeTime(past, now))
    }

    @Test
    fun `formatRelativeTime returns 1 год назад`() {
        val now = Instant.parse("2025-06-17T12:00:00Z")
        val past = Instant.parse("2024-06-17T12:00:00Z")
        assertEquals("1 год назад", formatRelativeTime(past, now))
    }

    @Test
    fun `formatRelativeTime returns 2 года назад`() {
        val now = Instant.parse("2025-06-17T12:00:00Z")
        val past = Instant.parse("2023-06-17T12:00:00Z")
        assertEquals("2 года назад", formatRelativeTime(past, now))
    }

    @Test
    fun `formatRelativeTime returns 5 лет назад`() {
        val now = Instant.parse("2025-06-17T12:00:00Z")
        val past = Instant.parse("2020-06-17T12:00:00Z")
        assertEquals("5 лет назад", formatRelativeTime(past, now))
    }

    @Test
    fun `formatRelativeTime returns только что when past is in future`() {
        val now = Instant.parse("2025-06-17T12:00:00Z")
        val past = Instant.parse("2025-06-17T13:00:00Z")
        assertEquals("только что", formatRelativeTime(past, now))
    }
}
