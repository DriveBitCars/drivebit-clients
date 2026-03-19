@file:OptIn(kotlin.time.ExperimentalTime::class)
package my.drivebit.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

fun dateToStartAtIso(date: String?): String? {
    val d = date?.take(10) ?: return null
    if (d.length != 10) return null
    return runCatching {
        LocalDateTime.parse("${d}T10:00:00").toInstant(TimeZone.currentSystemDefault()).toString()
    }.getOrNull()
}

fun dateToEndAtIso(date: String?): String? {
    val d = date?.take(10) ?: return null
    if (d.length != 10) return null
    return runCatching {
        LocalDateTime.parse("${d}T10:00:00").toInstant(TimeZone.currentSystemDefault()).toString()
    }.getOrNull()
}
