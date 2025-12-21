package my.drivebit.viewmodels

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.repositories.AvatarRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeAvatarRepository : AvatarRepository {
    private val _avatarUrl = kotlinx.coroutines.flow.MutableStateFlow("images/menu/user.svg")
    override val avatarUrl: kotlinx.coroutines.flow.Flow<String> = _avatarUrl

    fun setAvatarUrl(url: String) {
        _avatarUrl.value = url
    }

    override fun clearCache() {
    }

    override suspend fun refresh() {
        // No-op for testing
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
            val viewModel = IconUserViewModel(fakeRepository)

            advanceUntilIdle()
            val url = viewModel.avatarUrl.first()

            assertEquals("https://example.com/avatar.jpg", url)
        }

    @Test
    fun `avatarUrl should update when repository updates`() =
        runTest(StandardTestDispatcher()) {
            val fakeRepository = FakeAvatarRepository()
            val viewModel = IconUserViewModel(fakeRepository)

            advanceUntilIdle()
            val initialUrl = viewModel.avatarUrl.first()
            assertEquals("images/menu/user.svg", initialUrl)

            fakeRepository.setAvatarUrl("https://example.com/new-avatar.jpg")
            advanceUntilIdle()

            val updatedUrl = viewModel.avatarUrl.first()
            assertEquals("https://example.com/new-avatar.jpg", updatedUrl)
        }
}
