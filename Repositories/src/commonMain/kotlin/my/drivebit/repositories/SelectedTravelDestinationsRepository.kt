package my.drivebit.repositories

import com.russhwolf.settings.Settings

interface SelectedTravelDestinationsRepository {
    fun saveDestinations(
        names: List<String>,
        translates: List<String>,
    )

    fun getDestinationNames(): List<String>

    fun getDestinationTranslates(): List<String>

    fun clearDestinations()
}

internal class SelectedTravelDestinationsRepositoryImpl(
    private val settings: Settings,
) : SelectedTravelDestinationsRepository {
    companion object {
        private const val NAMES_KEY = "selected_travel_destination_names"
        private const val TRANSLATES_KEY = "selected_travel_destination_translates"
        private const val SEPARATOR = "\u001f"
    }

    override fun saveDestinations(
        names: List<String>,
        translates: List<String>,
    ) {
        settings.putString(NAMES_KEY, names.joinToString(SEPARATOR))
        settings.putString(TRANSLATES_KEY, translates.joinToString(SEPARATOR))
    }

    override fun getDestinationNames(): List<String> = decodeList(NAMES_KEY)

    override fun getDestinationTranslates(): List<String> = decodeList(TRANSLATES_KEY)

    override fun clearDestinations() {
        settings.remove(NAMES_KEY)
        settings.remove(TRANSLATES_KEY)
    }

    private fun decodeList(key: String): List<String> {
        val raw = settings.getStringOrNull(key)?.takeIf { it.isNotEmpty() } ?: return emptyList()
        return raw.split(SEPARATOR).filter { it.isNotEmpty() }
    }
}
