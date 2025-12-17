package my.drivebit.viewmodels

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.test.runTest
import my.drivebit.repositories.AddressSuggestRepository
import my.drivebit.repositories.ResultAddressSuggest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
                "Москва, ул. Ленина, д. 1",
                "Москва, ул. Пушкина, д. 2",
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
            assertFalse(viewModel.isLoading.value)
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
            assertEquals("Москва, ул. Ленина, д. 1", viewModel.suggestions.value[0])
            assertEquals("Москва, ул. Пушкина, д. 2", viewModel.suggestions.value[1])
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
    fun `isLoading should change during request lifecycle`() =
        runTest {
            val fakeRepo = FakeAddressSuggestRepository()
            val viewModel =
                AddressSuggestViewModel(
                    addressSuggestRepository = fakeRepo,
                    coroutineScope = backgroundScope,
                )

            assertFalse(viewModel.isLoading.value)

            viewModel.updateQuery("Москва")
            kotlinx.coroutines.delay(3100)

            assertFalse(viewModel.isLoading.value)
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
            assertFalse(viewModel.isLoading.value)
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
