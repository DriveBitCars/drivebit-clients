package my.drivebit.network.services

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable
import my.drivebit.network.DEFAULT_BASE_URL
import my.drivebit.network.parseResponse

interface Dictionary {
    suspend fun getCarBrands(): List<CarBrand>
    suspend fun searchCities(query: String): List<City>
}

@Serializable
data class CarBrand(
    val id: Int,
    val apiId: String? = null,
    val name: String,
    val cyrillicName: String? = null,
    val country: String? = null,
)

@Serializable
data class City(
    val id: Int,
    val name: String,
)

class DictionaryImpl(
    private val httpClient: HttpClient,
) : Dictionary {
    override suspend fun getCarBrands(): List<CarBrand> {
        val url = "${DEFAULT_BASE_URL}Dictionary/cars/brands"
        val response = httpClient.get(url)
        return response.parseResponse()
    }

    override suspend fun searchCities(query: String): List<City> {
        val url = "${DEFAULT_BASE_URL}Dictionary/cities/search"
        val response = httpClient.get(url) {
            parameter("query", query)
        }
        return response.parseResponse()
    }
}

