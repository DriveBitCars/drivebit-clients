package my.drivebit.repositories

import my.drivebit.network.services.City
import my.drivebit.network.services.Dictionary

interface CityRepository {
    suspend fun searchCities(query: String): ResultCities

    suspend fun getAllCities(): ResultCities
}

sealed interface ResultCities {
    data class Success(
        val cities: List<City>,
    ) : ResultCities

    data class Error(
        val message: String,
    ) : ResultCities
}

class CityRepositoryImpl(
    private val dictionary: Dictionary,
) : CityRepository {
    override suspend fun searchCities(query: String): ResultCities =
        runCatching {
            dictionary.searchCities(query)
        }.fold(
            onSuccess = { cities -> ResultCities.Success(cities) },
            onFailure = { exception ->
                ResultCities.Error(exception.message ?: "Unknown error")
            },
        )

    override suspend fun getAllCities(): ResultCities =
        runCatching {
            dictionary.getAllCities()
        }.fold(
            onSuccess = { cities -> ResultCities.Success(cities) },
            onFailure = { exception ->
                ResultCities.Error(exception.message ?: "Unknown error")
            },
        )
}
