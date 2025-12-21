package my.drivebit.repositories

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.AddressData
import my.drivebit.network.services.AddressSuggestion
import my.drivebit.network.services.Dadata
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeDadata : Dadata {
    var shouldThrow = false
    var errorMessage: String? = "Network error"
    var lastQuery: String? = null

    override suspend fun suggest(query: String): List<AddressSuggestion> {
        lastQuery = query
        if (shouldThrow) {
            throw if (errorMessage != null) Exception(errorMessage) else Exception()
        }
        return listOf(
            AddressSuggestion(
                value = "Москва, ул. Ленина, д. 1",
                unrestrictedValue = "Москва, ул. Ленина, д. 1",
                data = AddressData(geoLat = "55.7558", geoLon = "37.6173"),
            ),
            AddressSuggestion(
                value = "Москва, ул. Пушкина, д. 2",
                unrestrictedValue = "Москва, ул. Пушкина, д. 2",
                data = AddressData(geoLat = "55.7559", geoLon = "37.6174"),
            ),
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AddressSuggestRepositoryTest {
    @Test
    fun `suggest should return Success with suggestions on success`() =
        runTest {
            val fakeDadata = FakeDadata()
            val repo = AddressSuggestRepositoryImpl(fakeDadata)

            val result = repo.suggest("Ленина", "Москва")

            assertTrue(result is ResultAddressSuggest.Success)
            assertEquals(2, result.suggestions.size)
            assertEquals("Москва, ул. Ленина, д. 1", result.suggestions[0].value)
            assertEquals("Москва, ул. Пушкина, д. 2", result.suggestions[1].value)
            assertEquals("Москва Ленина", fakeDadata.lastQuery)
        }

    @Test
    fun `suggest should return Error with message from exception`() =
        runTest {
            val fakeDadata =
                FakeDadata().apply {
                    shouldThrow = true
                    errorMessage = "Invalid query"
                }
            val repo = AddressSuggestRepositoryImpl(fakeDadata)

            val result = repo.suggest("invalid", "Москва")

            assertTrue(result is ResultAddressSuggest.Error)
            assertEquals("Invalid query", result.message)
        }

    @Test
    fun `suggest should return default error message when exception message is null or blank`() =
        runTest {
            val fakeDadata =
                FakeDadata().apply {
                    shouldThrow = true
                    errorMessage = ""
                }
            val repo = AddressSuggestRepositoryImpl(fakeDadata)

            val result = repo.suggest("any", "Москва")

            assertTrue(result is ResultAddressSuggest.Error)
            assertEquals("Произошла ошибка", result.message)
        }

    @Test
    fun `suggest should filter out suggestions without coordinates`() =
        runTest {
            val fakeDadata =
                object : Dadata {
                    override suspend fun suggest(query: String): List<AddressSuggestion> =
                        listOf(
                            AddressSuggestion(
                                value = "Москва, ул. Ленина",
                                unrestrictedValue = null,
                                data = AddressData(geoLat = "55.7558", geoLon = "37.6173"),
                            ),
                            AddressSuggestion(
                                value = null,
                                unrestrictedValue = "Москва, ул. Пушкина",
                                data = AddressData(geoLat = "55.7559", geoLon = "37.6174"),
                            ),
                            AddressSuggestion(
                                value = "Москва, ул. Гагарина",
                                unrestrictedValue = null,
                                data = AddressData(geoLat = "55.7560", geoLon = "37.6175"),
                            ),
                            AddressSuggestion(
                                value = "Москва, ул. Без координат",
                                unrestrictedValue = null,
                                data = AddressData(geoLat = null, geoLon = null),
                            ),
                        )
                }
            val repo = AddressSuggestRepositoryImpl(fakeDadata)

            val result = repo.suggest("Ленина", "Москва")

            assertTrue(result is ResultAddressSuggest.Success)
            assertEquals(3, result.suggestions.size)
            assertEquals("Москва, ул. Ленина", result.suggestions[0].value)
            assertEquals(null, result.suggestions[1].value)
            assertEquals("Москва, ул. Гагарина", result.suggestions[2].value)
        }
}
