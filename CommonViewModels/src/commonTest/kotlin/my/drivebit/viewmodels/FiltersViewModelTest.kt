package my.drivebit.viewmodels

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FiltersViewModelTest {
    @Test
    fun `initial state should have filter catalog`() =
        runTest {
            val viewModel = FiltersViewModel()

            val initialState = viewModel.state.first()

            assertEquals(11, initialState.filters.size)
            assertEquals("Все", initialState.filters[0].title)
            assertEquals("Поблизости", initialState.filters[1].title)
            assertEquals("В Крым", initialState.filters[2].title)
        }

    @Test
    fun `filters list stays stable`() =
        runTest {
            val viewModel = FiltersViewModel()
            val first = viewModel.state.value.filters
            val second = viewModel.state.value.filters
            assertEquals(first, second)
        }
}
