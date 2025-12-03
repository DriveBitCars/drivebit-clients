package my.drivebit.viewmodels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import my.drivebit.resources.ImagePaths
import my.drivebit.shared.storage.Storage

sealed interface ButterState {
    data object Idle : ButterState

    data class Opened(
        val model: List<ButterModel>,
    ) : ButterState
}

data class ButterModel(
    val iconUrl: String? = null,
    val text: String,
    val onClick: () -> Unit,
)

interface ButterViewModel {
    val state: StateFlow<ButterState>

    fun open()

    fun close()

    fun onClick()
}

private val login =
    ButterModel(
        text = "Логин",
        onClick = {},
    )

private val registr =
    ButterModel(
        text = "Регистрация",
        onClick = {},
    )

private val profile =
    ButterModel(
        iconUrl = ImagePaths.MENU_USER_SVG, // TODO profile.userIconUrl
        text = "Мой профиль",
        onClick = {},
    )

private val beCameAHost =
    ButterModel(
        iconUrl = ImagePaths.BUTTER_CAR_ICON_SVG,
        text = "Сдать авто",
        onClick = {},
    )

private val logout =
    ButterModel(
        iconUrl = ImagePaths.BUTTER_LOGOUT_SVG,
        text = "Выйти",
        onClick = {},
    )

class ButterViewModelImpl(
    private val storage: Storage,
) : ButterViewModel {
    private val _state = MutableStateFlow<ButterState>(ButterState.Idle)

    override val state: StateFlow<ButterState>
        get() = _state.asStateFlow()

    override fun open() {
        _state.value =
            ButterState.Opened(
                buildList {
                    if (storage.isLogined()) {
                        add(profile.copy(onClick = { close() }))
                    } else {
                        add(login.copy(onClick = { close() }))
                        add(registr.copy(onClick = { close() }))
                    }
                    add(beCameAHost.copy(onClick = { close() }))
                    if (storage.isLogined()) {
                        add(
                            logout.copy(onClick = {
                                logout()
                                close()
                            }),
                        )
                    }
                },
            )
    }

    private fun logout() {
        storage.logout()
    }

    override fun close() {
        _state.value = ButterState.Idle
    }

    override fun onClick() {
        when (_state.value) {
            is ButterState.Idle -> open()
            is ButterState.Opened -> close()
        }
    }
}
