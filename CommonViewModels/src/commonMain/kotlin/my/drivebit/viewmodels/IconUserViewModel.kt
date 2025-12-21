package my.drivebit.viewmodels

import my.drivebit.repositories.AvatarRepository

class IconUserViewModel(
    private val avatarRepository: AvatarRepository,
) {
    val avatarUrl
        get() = avatarRepository.avatarUrl

    fun refresh() {
        avatarRepository.clearCache()
    }
}
