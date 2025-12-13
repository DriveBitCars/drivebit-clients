package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import my.drivebit.repositories.AvatarRepository
import my.drivebit.utils.DEFAULT_AVATAR_PATH

class IconUserViewModel(
    private val avatarRepository: AvatarRepository,
) {
    val avatarUrl
        get() = avatarRepository.avatarUrl

    fun refresh() {
        avatarRepository.refresh()
    }
}
