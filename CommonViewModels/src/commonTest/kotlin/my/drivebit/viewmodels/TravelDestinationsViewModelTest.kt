package my.drivebit.viewmodels

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.repositories.EnumItem
import my.drivebit.repositories.SelectedTravelDestinationsRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TravelDestinationsViewModelTest {
    @Test
    fun `load exposes travel destinations from enums repository`() =
        runTest(StandardTestDispatcher()) {
            val enums =
                object : FakeCarEnumsForTravel() {
                    override suspend fun getAllTravelDestinations(): List<EnumItem> =
                        listOf(
                            EnumItem(1, "Belarus", "Беларусь"),
                            EnumItem(2, "Crimea", "В Крым"),
                        )
                }
            val selected = FakeSelectedTravelDestinations()
            val viewModel = TravelDestinationsViewModel(enums, selected, this)

            viewModel.load()
            advanceUntilIdle()

            assertEquals(2, viewModel.options.value.size)
            assertEquals("Belarus", viewModel.options.value[0].name)
        }

    @Test
    fun `toggle and confirmSelection persist selected names`() =
        runTest(StandardTestDispatcher()) {
            val enums =
                object : FakeCarEnumsForTravel() {
                    override suspend fun getAllTravelDestinations(): List<EnumItem> =
                        listOf(
                            EnumItem(1, "Belarus", "Беларусь"),
                            EnumItem(3, "Abkhazia", "Абхазия"),
                        )
                }
            val selected = FakeSelectedTravelDestinations()
            val viewModel = TravelDestinationsViewModel(enums, selected, this)

            viewModel.load()
            advanceUntilIdle()
            viewModel.toggle("Belarus")
            viewModel.toggle("Abkhazia")
            viewModel.toggle("Belarus")
            viewModel.confirmSelection()

            assertEquals(listOf("Abkhazia"), selected.getDestinationNames())
            assertEquals(listOf("Абхазия"), selected.getDestinationTranslates())
            assertEquals(listOf("Abkhazia"), viewModel.selectedNames.value)
        }

    @Test
    fun `load restores previously saved destinations`() =
        runTest(StandardTestDispatcher()) {
            val enums =
                object : FakeCarEnumsForTravel() {
                    override suspend fun getAllTravelDestinations(): List<EnumItem> =
                        listOf(EnumItem(2, "Crimea", "В Крым"))
                }
            val selected =
                FakeSelectedTravelDestinations().apply {
                    saveDestinations(listOf("Crimea"), listOf("В Крым"))
                }
            val viewModel = TravelDestinationsViewModel(enums, selected, this)

            viewModel.load()
            advanceUntilIdle()

            assertEquals(listOf("Crimea"), viewModel.selectedNames.value)
            assertTrue(viewModel.error.value == null)
        }
}

private open class FakeCarEnumsForTravel : my.drivebit.repositories.CarEnumsRepository by createMockCarEnumsRepository()

private class FakeSelectedTravelDestinations : SelectedTravelDestinationsRepository {
    private var names: List<String> = emptyList()
    private var translates: List<String> = emptyList()

    override fun saveDestinations(
        names: List<String>,
        translates: List<String>,
    ) {
        this.names = names
        this.translates = translates
    }

    override fun getDestinationNames(): List<String> = names

    override fun getDestinationTranslates(): List<String> = translates

    override fun clearDestinations() {
        names = emptyList()
        translates = emptyList()
    }
}
