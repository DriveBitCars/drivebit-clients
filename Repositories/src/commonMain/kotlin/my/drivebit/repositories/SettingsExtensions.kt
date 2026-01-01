package my.drivebit.repositories

import com.russhwolf.settings.Settings

internal fun Settings.getStringOrNullIfEmpty(key: String): String? = getStringOrNull(key)?.takeIf { it.isNotEmpty() }
