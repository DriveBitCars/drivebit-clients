package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.repositories.AvatarRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeAvatarRepository : AvatarRepository {
    private val _avatarUrl = kotlinx.coroutines.flow.MutableStateFlow("images/menu/user.svg")
    override val avatarUrl: kotlinx.coroutines.flow.Flow<String> = _avatarUrl

    fun setAvatarUrl(url: String) {
        _avatarUrl.value = url
    }

    override fun refresh() {
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class IconUserViewModelTest {
    @Test
    fun `avatarUrl should return value from repository`() =
        runTest(StandardTestDispatcher()) {
            val fakeRepository =
                FakeAvatarRepository().apply {
                    setAvatarUrl("https://example.com/avatar.jpg")
                }
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val viewModel = IconUserViewModel(fakeRepository, testScope)

            advanceUntilIdle()
            val url =
                viewModel.avatarUrl
                    .take(2)
                    .toList()
                    .last()

            assertEquals("https://example.com/avatar.jpg", url)
            testScope.coroutineContext.cancelChildren()
        }

    @Test
    fun `avatarUrl should update when repository updates`() =
        runTest(StandardTestDispatcher()) {
            val fakeRepository = FakeAvatarRepository()
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val viewModel = IconUserViewModel(fakeRepository, testScope)

            advanceUntilIdle()
            val initialUrl = viewModel.avatarUrl.first()
            assertEquals("images/menu/user.svg", initialUrl)

            fakeRepository.setAvatarUrl("https://example.com/new-avatar.jpg")
            advanceUntilIdle()

            val updatedUrl = viewModel.avatarUrl.first()
            assertEquals("https://example.com/new-avatar.jpg", updatedUrl)
            testScope.coroutineContext.cancelChildren()
        }

    @Test
    fun `refresh should call repository refresh`() =
        runTest(StandardTestDispatcher()) {
            var refreshCalled = false
            val fakeRepository =
                object : AvatarRepository {
                    private val _avatarUrl = kotlinx.coroutines.flow.MutableStateFlow("images/menu/user.svg")
                    override val avatarUrl: kotlinx.coroutines.flow.Flow<String> = _avatarUrl

                    override fun refresh() {
                        refreshCalled = true
                    }
                }
            val testScope = CoroutineScope(SupervisorJob() + this.coroutineContext)
            val viewModel = IconUserViewModel(fakeRepository, testScope)

            viewModel.refresh()

            assertTrue(refreshCalled)
            testScope.coroutineContext.cancelChildren()
        }
}
