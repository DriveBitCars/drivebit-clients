package my.drivebit.web

import my.drivebit.network.services.City
import my.drivebit.network.services.Dictionary
import my.drivebit.utils.cityNameToSlug

private const val MOSCOW_NAME = "Москва"

/**
 * Maps URL slug → [City] using [Dictionary.getAllCities]. On slug collision, the city with the
 * smaller [City.id] wins.
 */
class CitySlugResolver(
    private val dictionary: Dictionary,
) {
    private var slugToCity: Map<String, City>? = null

    private suspend fun ensureLoaded() {
        if (slugToCity != null) return
        val cities = dictionary.getAllCities().sortedBy { it.id }
        slugToCity =
            buildMap {
                for (city in cities) {
                    val slug = cityNameToSlug(city.name)
                    if (slug !in this) put(slug, city)
                }
            }
    }

    suspend fun resolve(slug: String): City? {
        ensureLoaded()
        return slugToCity!![slug.lowercase()]
    }

    suspend fun moscowCity(): City {
        ensureLoaded()
        slugToCity!!["moskva"]?.let { return it }
        val found =
            dictionary
                .searchCities(MOSCOW_NAME)
                .firstOrNull { it.name == MOSCOW_NAME }
        requireNotNull(found) { "Could not resolve Moscow for city path fallback" }
        return found
    }
}
