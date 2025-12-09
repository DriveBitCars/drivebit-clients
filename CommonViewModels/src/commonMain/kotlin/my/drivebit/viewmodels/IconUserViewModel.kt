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
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    val avatarUrl: StateFlow<String> =
        avatarRepository.avatarUrl.stateIn(
            scope = coroutineScope,
            started = SharingStarted.Lazily,
            initialValue = DEFAULT_AVATAR_PATH,
        )

    fun refresh() {
        avatarRepository.refresh()
    }
}
