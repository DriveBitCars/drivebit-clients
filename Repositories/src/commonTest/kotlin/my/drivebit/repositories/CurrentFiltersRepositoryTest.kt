package my.drivebit.repositories

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import my.drivebit.shared.storage.InMemorySettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CurrentFiltersRepositoryTest {
    @Test
    fun `should return null by default for currentTaskShortName`() =
        runTest {
            val repository = CurrentFiltersRepositoryImpl(InMemorySettings())

            val result = repository.currentTaskShortName.first()

            assertNull(result)
        }

    @Test
    fun `should return null by default for startState`() =
        runTest {
            val repository = CurrentFiltersRepositoryImpl(InMemorySettings())

            val result = repository.startState.first()

            assertNull(result)
        }

    @Test
    fun `should return null by default for endState`() =
        runTest {
            val repository = CurrentFiltersRepositoryImpl(InMemorySettings())

            val result = repository.endState.first()

            assertNull(result)
        }

    @Test
    fun `should set and retrieve current task short name`() =
        runTest {
            val repository = CurrentFiltersRepositoryImpl(InMemorySettings())
            val testShortName = "TASK-123"

            repository.updateCurrentTask(testShortName)
            val result = repository.currentTaskShortName.first()

            assertEquals(testShortName, result)
        }

    @Test
    fun `should set and retrieve start date`() =
        runTest {
            val repository = CurrentFiltersRepositoryImpl(InMemorySettings())
            val testStartDate = "2025-02-01"

            repository.updateStartDate(testStartDate)
            val result = repository.startState.first()

            assertEquals(testStartDate, result)
        }

    @Test
    fun `should set and retrieve end date`() =
        runTest {
            val repository = CurrentFiltersRepositoryImpl(InMemorySettings())
            val testEndDate = "2025-02-15"

            repository.updateEndDate(testEndDate)
            val result = repository.endState.first()

            assertEquals(testEndDate, result)
        }

    @Test
    fun `should update current task short name`() =
        runTest {
            val repository = CurrentFiltersRepositoryImpl(InMemorySettings())
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
    fun `should update start date`() =
        runTest {
            val repository = CurrentFiltersRepositoryImpl(InMemorySettings())
            val firstStartDate = "2025-02-01"
            val secondStartDate = "2025-02-10"

            repository.updateStartDate(firstStartDate)
            val firstResult = repository.startState.first()
            assertEquals(firstStartDate, firstResult)

            repository.updateStartDate(secondStartDate)
            val secondResult = repository.startState.first()
            assertEquals(secondStartDate, secondResult)
        }

    @Test
    fun `should update end date`() =
        runTest {
            val repository = CurrentFiltersRepositoryImpl(InMemorySettings())
            val firstEndDate = "2025-02-15"
            val secondEndDate = "2025-02-20"

            repository.updateEndDate(firstEndDate)
            val firstResult = repository.endState.first()
            assertEquals(firstEndDate, firstResult)

            repository.updateEndDate(secondEndDate)
            val secondResult = repository.endState.first()
            assertEquals(secondEndDate, secondResult)
        }

    @Test
    fun `should not persist currentTaskShortName across repository instances`() =
        runTest {
            val settings = InMemorySettings()
            val firstRepository = CurrentFiltersRepositoryImpl(settings)
            val testShortName = "TASK-789"

            firstRepository.updateCurrentTask(testShortName)

            val secondRepository = CurrentFiltersRepositoryImpl(settings)
            val result = secondRepository.currentTaskShortName.first()

            assertNull(result)
        }

    @Test
    fun `should persist startDate across repository instances`() =
        runTest {
            val settings = InMemorySettings()
            val firstRepository = CurrentFiltersRepositoryImpl(settings)
            val testStartDate = "2025-02-01"

            firstRepository.updateStartDate(testStartDate)

            val secondRepository = CurrentFiltersRepositoryImpl(settings)
            val result = secondRepository.startState.first()

            assertEquals(testStartDate, result)
        }

    @Test
    fun `should persist endDate across repository instances`() =
        runTest {
            val settings = InMemorySettings()
            val firstRepository = CurrentFiltersRepositoryImpl(settings)
            val testEndDate = "2025-02-15"

            firstRepository.updateEndDate(testEndDate)

            val secondRepository = CurrentFiltersRepositoryImpl(settings)
            val result = secondRepository.endState.first()

            assertEquals(testEndDate, result)
        }

    @Test
    fun `should allow setting start date to null`() =
        runTest {
            val repository = CurrentFiltersRepositoryImpl(InMemorySettings())
            val testStartDate = "2025-02-01"

            repository.updateStartDate(testStartDate)
            assertEquals(testStartDate, repository.startState.first())

            repository.updateStartDate(null)
            assertNull(repository.startState.first())
        }

    @Test
    fun `should allow setting end date to null`() =
        runTest {
            val repository = CurrentFiltersRepositoryImpl(InMemorySettings())
            val testEndDate = "2025-02-15"

            repository.updateEndDate(testEndDate)
            assertEquals(testEndDate, repository.endState.first())

            repository.updateEndDate(null)
            assertNull(repository.endState.first())
        }
}
