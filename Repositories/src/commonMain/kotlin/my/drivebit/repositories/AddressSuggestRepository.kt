package my.drivebit.repositories

import my.drivebit.network.services.Dadata

interface AddressSuggestRepository {
    suspend fun suggest(query: String, city: String): ResultAddressSuggest
}

sealed interface ResultAddressSuggest {
    data class Success(
        val suggestions: List<String>,
    ) : ResultAddressSuggest

    data class Error(
        val message: String,
    ) : ResultAddressSuggest
}

internal class AddressSuggestRepositoryImpl(
    private val dadata: Dadata,
) : AddressSuggestRepository {
    override suspend fun suggest(query: String, city: String): ResultAddressSuggest {
        val result =
            runCatching {
                dadata.suggest(query, city)
            }

        return result.fold(
            onSuccess = { response ->
                val suggestions = response
                    .filter { suggestion ->
                        suggestion.data?.geoLat != null &&
                            suggestion.data?.geoLon != null &&
                            suggestion.data?.geoLat?.isNotBlank() == true &&
                            suggestion.data?.geoLon?.isNotBlank() == true
                    }
                    .mapNotNull { it.value }
                ResultAddressSuggest.Success(suggestions)
            },
            onFailure = { throwable ->
                val errorMessage = throwable.message?.takeIf { it.isNotBlank() } ?: "Произошла ошибка"
                ResultAddressSuggest.Error(errorMessage)
            },
        )
    }
}
