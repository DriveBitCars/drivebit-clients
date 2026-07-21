package my.drivebit.repositories

import my.drivebit.shared.storage.InMemorySettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SelectedTravelDestinationsRepositoryTest {
    @Test
    fun `save and get destinations preserves names and translates`() {
        val repository = SelectedTravelDestinationsRepositoryImpl(InMemorySettings())

        repository.saveDestinations(
            names = listOf("Belarus", "Crimea"),
            translates = listOf("Беларусь", "В Крым"),
        )

        assertEquals(listOf("Belarus", "Crimea"), repository.getDestinationNames())
        assertEquals(listOf("Беларусь", "В Крым"), repository.getDestinationTranslates())
    }

    @Test
    fun `get destinations returns empty when nothing saved`() {
        val repository = SelectedTravelDestinationsRepositoryImpl(InMemorySettings())

        assertTrue(repository.getDestinationNames().isEmpty())
        assertTrue(repository.getDestinationTranslates().isEmpty())
    }

    @Test
    fun `clear destinations removes saved values`() {
        val repository = SelectedTravelDestinationsRepositoryImpl(InMemorySettings())
        repository.saveDestinations(
            names = listOf("Abkhazia"),
            translates = listOf("Абхазия"),
        )

        repository.clearDestinations()

        assertTrue(repository.getDestinationNames().isEmpty())
        assertTrue(repository.getDestinationTranslates().isEmpty())
    }
}
