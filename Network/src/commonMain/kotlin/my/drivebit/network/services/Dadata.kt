package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import my.drivebit.network.DEFAULT_BASE_URL
import my.drivebit.network.parseResponse

interface Dadata {
    suspend fun suggest(
        query: String,
        city: String,
    ): List<AddressSuggestion>
}

@Serializable
data class AddressSuggestRequest(
    val query: String,
    val count: Int = 10,
    val city: String,
)

@Serializable
data class AddressSuggestion(
    val value: String? = null,
    val unrestrictedValue: String? = null,
    val data: AddressData? = null,
)

@Serializable
data class AddressData(
    val postalCode: String? = null,
    val region: String? = null,
    val city: String? = null,
    val street: String? = null,
    val house: String? = null,
    val geoLat: String? = null,
    val geoLon: String? = null,
    val fiasId: String? = null,
)

class DadataImpl(
    private val httpClient: HttpClient,
) : Dadata {
    override suspend fun suggest(
        query: String,
        city: String,
    ): List<AddressSuggestion> {
        val url = "${DEFAULT_BASE_URL}api/Dadata/suggest"
        val request = AddressSuggestRequest(query = query, city = city)
        val response =
            httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

        return response.parseResponse()
    }
}
