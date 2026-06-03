package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class MainContentViewModelTest {
    private fun createViewModel(
        repository: MockSearchCarSearchRepository,
        dispatcher: CoroutineDispatcher,
    ): MainContentViewModelImpl =
        MainContentViewModelImpl(
            carSearchRepository = repository,
            coroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
        )

    @Test
    fun `firstList emits FirstList after repository returns data`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val viewModel = createViewModel(MockSearchCarSearchRepository(), dispatcher)

            assertIs<MainContentListState.Loading>(viewModel.firstList.value)

            advanceUntilIdle()

            assertIs<MainContentListState.FirstList>(viewModel.firstList.value)
        }

    @Test
    fun `markSearchStarted sets Loading`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val viewModel = createViewModel(MockSearchCarSearchRepository(), dispatcher)

            advanceUntilIdle()
            assertIs<MainContentListState.FirstList>(viewModel.firstList.value)

            viewModel.markSearchStarted()
            assertIs<MainContentListState.Loading>(viewModel.firstList.value)
        }

    @Test
    fun `repository error sets Error state`() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val repository = MockSearchCarSearchRepository()
            repository.shouldThrowError = true
            val viewModel = createViewModel(repository, dispatcher)

            advanceUntilIdle()

            assertIs<MainContentListState.Error>(viewModel.firstList.value)
        }
}
