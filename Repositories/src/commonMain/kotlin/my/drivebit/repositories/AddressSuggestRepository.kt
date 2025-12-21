package my.drivebit.repositories

import my.drivebit.network.services.AddressSuggestion
import my.drivebit.network.services.Dadata

interface AddressSuggestRepository {
    suspend fun suggest(
        query: String,
        city: String,
    ): ResultAddressSuggest
}

sealed interface ResultAddressSuggest {
    data class Success(
        val suggestions: List<AddressSuggestion>,
    ) : ResultAddressSuggest

    data class Error(
        val message: String,
    ) : ResultAddressSuggest
}

internal class AddressSuggestRepositoryImpl(
    private val dadata: Dadata,
) : AddressSuggestRepository {
    override suspend fun suggest(
        query: String,
        city: String,
    ): ResultAddressSuggest {
        val queryWithCity = "$city $query".trim()
        val result =
            runCatching {
                dadata.suggest(queryWithCity)
            }

        return result.fold(
            onSuccess = { response ->
                val suggestions =
                    response
                        .filter { suggestion ->
                            suggestion.data?.geoLat != null &&
                                suggestion.data?.geoLon != null &&
                                suggestion.data?.geoLat?.isNotBlank() == true &&
                                suggestion.data?.geoLon?.isNotBlank() == true
                        }
                ResultAddressSuggest.Success(suggestions)
            },
            onFailure = { throwable ->
                val errorMessage = throwable.message?.takeIf { it.isNotBlank() } ?: "Произошла ошибка"
                ResultAddressSuggest.Error(errorMessage)
            },
        )
    }
}
