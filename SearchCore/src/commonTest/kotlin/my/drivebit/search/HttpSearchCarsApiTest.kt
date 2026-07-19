package my.drivebit.search

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import my.drivebit.network.NetworkException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class HttpSearchCarsApiTest {
    @Test
    fun `searchCars throws NetworkException on 503 without content type`() =
        runTest {
            val mockEngine =
                MockEngine {
                    respond(
                        content = "",
                        status = HttpStatusCode.ServiceUnavailable,
                        headers = headersOf(),
                    )
                }
            val api = HttpSearchCarsApi(HttpClient(mockEngine))

            val exception =
                assertFailsWith<NetworkException> {
                    api.searchCars(
                        cityId = "158835",
                        dateFrom = null,
                        dateTo = null,
                        availableMileagePerDayKmMin = null,
                        dailyPriceMin = null,
                        dailyPriceMax = null,
                        yearMin = null,
                        yearMax = null,
                        seatsMin = null,
                        bodyTypes = null,
                        brandId = 1,
                        modelId = null,
                        driveTypes = null,
                        geoLat = null,
                        geoLon = null,
                        radiusKm = null,
                        page = 1,
                        pageSize = 9,
                    )
                }

            assertEquals(HttpStatusCode.ServiceUnavailable, exception.statusCode)
        }
}
