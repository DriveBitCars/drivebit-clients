package my.drivebit.repositories

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import my.drivebit.shared.storage.InMemorySettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CurrentTaskRepositoryTest {
    @Test
    fun `should return null by default`() =
        runTest {
            val repository = CurrentTaskRepositoryImpl(InMemorySettings())

            val result = repository.currentTaskShortName.first()

            assertNull(result)
        }

    @Test
    fun `should set and retrieve current task short name`() =
        runTest {
            val repository = CurrentTaskRepositoryImpl(InMemorySettings())
            val testShortName = "TASK-123"

            repository.updateCurrentTask(testShortName)
            val result = repository.currentTaskShortName.first()

            assertEquals(testShortName, result)
        }

    @Test
    fun `should update current task short name`() =
        runTest {
            val repository = CurrentTaskRepositoryImpl(InMemorySettings())
            val firstShortName = "TASK-123"
            val secondShortName = "TASK-456"

            repository.updateCurrentTask(firstShortName)
            val firstResult = repository.currentTaskShortName.first()
            assertEquals(firstShortName, firstResult)

            repository.updateCurrentTask(secondShortName)
            val secondResult = repository.currentTaskShortName.first()
            assertEquals(secondShortName, secondResult)
        }

    @Test
    fun `should persist value across repository instances`() =
        runTest {
            val settings = InMemorySettings()
            val firstRepository = CurrentTaskRepositoryImpl(settings)
            val testShortName = "TASK-789"

            firstRepository.updateCurrentTask(testShortName)

            val secondRepository = CurrentTaskRepositoryImpl(settings)
            val result = secondRepository.currentTaskShortName.first()

            assertEquals(testShortName, result)
        }
}
