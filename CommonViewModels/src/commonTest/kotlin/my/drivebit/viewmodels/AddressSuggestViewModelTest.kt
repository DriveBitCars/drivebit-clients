package my.drivebit.viewmodels

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.AddressData
import my.drivebit.network.services.AddressSuggestion
import my.drivebit.repositories.AddressSuggestRepository
import my.drivebit.repositories.ResultAddressSuggest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeAddressSuggestRepository : AddressSuggestRepository {
    var shouldReturnError = false
    var errorMessage: String = "Error"
    var lastQuery: String? = null
    var callCount = 0

    override suspend fun suggest(query: String): ResultAddressSuggest {
        lastQuery = query
        callCount++
        if (shouldReturnError) {
            return ResultAddressSuggest.Error(errorMessage)
        }
        return ResultAddressSuggest.Success(
            listOf(
                AddressSuggestion(
                    value = "Москва, ул. Ленина, д. 1",
                    data =
                        AddressData(
                            street = "Ленина",
                            house = "1",
                            geoLat = "55.7558",
                            geoLon = "37.6173",
                        ),
                ),
                AddressSuggestion(
                    value = "Москва, ул. Пушкина, д. 2",
                    data =
                        AddressData(
                            street = "Пушкина",
                            house = "2",
                            geoLat = "55.7558",
                            geoLon = "37.6173",
                        ),
                ),
            ),
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class AddressSuggestViewModelTest {
    @Test
    fun `initial state should have empty query and suggestions`() =
        runTest {
            val fakeRepo = FakeAddressSuggestRepository()
            val viewModel =
                AddressSuggestViewModel(
                    addressSuggestRepository = fakeRepo,
                    coroutineScope = backgroundScope,
                )

            assertEquals("", viewModel.query.value)
            assertTrue(viewModel.suggestions.value.isEmpty())
        }

    @Test
    fun `updateQuery should update query value`() =
        runTest {
            val fakeRepo = FakeAddressSuggestRepository()
            val viewModel =
                AddressSuggestViewModel(
                    addressSuggestRepository = fakeRepo,
                    coroutineScope = backgroundScope,
                )

            viewModel.updateQuery("Москва")

            assertEquals("Москва", viewModel.query.value)
        }

    @Test
    fun `suggestions should be updated after debounce delay`() =
        runTest {
            val fakeRepo = FakeAddressSuggestRepository()
            val viewModel =
                AddressSuggestViewModel(
                    addressSuggestRepository = fakeRepo,
                    coroutineScope = backgroundScope,
                )

            viewModel.updateQuery("Москва")
            assertTrue(viewModel.suggestions.value.isEmpty())

            kotlinx.coroutines.delay(3100)

            assertEquals(2, viewModel.suggestions.value.size)
            assertEquals("Москва, ул. Ленина, д. 1", viewModel.suggestions.value[0].value)
            assertEquals("Москва, ул. Пушкина, д. 2", viewModel.suggestions.value[1].value)
            assertEquals("Москва", fakeRepo.lastQuery)
        }

    @Test
    fun `should cancel previous request when new query is entered`() =
        runTest {
            val fakeRepo = FakeAddressSuggestRepository()
            val viewModel =
                AddressSuggestViewModel(
                    addressSuggestRepository = fakeRepo,
                    coroutineScope = backgroundScope,
                )

            viewModel.updateQuery("Москва")
            kotlinx.coroutines.delay(2000)
            viewModel.updateQuery("Санкт-Петербург")
            kotlinx.coroutines.delay(3100)

            assertEquals("Санкт-Петербург", fakeRepo.lastQuery)
        }

    @Test
    fun `should call repository with correct query`() =
        runTest {
            val fakeRepo = FakeAddressSuggestRepository()
            val viewModel =
                AddressSuggestViewModel(
                    addressSuggestRepository = fakeRepo,
                    coroutineScope = backgroundScope,
                )

            viewModel.updateQuery("Ленина")
            kotlinx.coroutines.delay(3100)

            assertEquals("Ленина", fakeRepo.lastQuery)
            assertEquals(1, fakeRepo.callCount)
        }

    @Test
    fun `should handle error result by keeping empty suggestions`() =
        runTest {
            val fakeRepo =
                FakeAddressSuggestRepository().apply {
                    shouldReturnError = true
                    errorMessage = "Network error"
                }
            val viewModel =
                AddressSuggestViewModel(
                    addressSuggestRepository = fakeRepo,
                    coroutineScope = backgroundScope,
                )

            viewModel.updateQuery("Москва")
            kotlinx.coroutines.delay(3100)

            assertTrue(viewModel.suggestions.value.isEmpty())
        }

    @Test
    fun `clearSuggestions should clear suggestions list`() =
        runTest {
            val fakeRepo = FakeAddressSuggestRepository()
            val viewModel =
                AddressSuggestViewModel(
                    addressSuggestRepository = fakeRepo,
                    coroutineScope = backgroundScope,
                )

            viewModel.updateQuery("Москва")
            kotlinx.coroutines.delay(3100)

            assertTrue(viewModel.suggestions.value.isNotEmpty())

            viewModel.clearSuggestions()

            assertTrue(viewModel.suggestions.value.isEmpty())
        }
}
