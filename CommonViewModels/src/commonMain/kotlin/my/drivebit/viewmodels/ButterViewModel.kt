package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import my.drivebit.repositories.AvatarRepository
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

private val myCars =
    ButterModel(
        iconUrl = ImagePaths.BUTTER_CAR_ICON_SVG,
        text = "Мои авто",
        onClick = {},
    )

private val myDocuments =
    ButterModel(
        iconUrl = ImagePaths.BUTTER_DOCS_SVG,
        text = "Мои документы",
        onClick = {},
    )

private val myBookings =
    ButterModel(
        iconUrl = ImagePaths.BUTTER_BOOKING_SVG,
        text = "Мои бронирования",
        onClick = {},
    )

private val myDeals =
    ButterModel(
        iconUrl = ImagePaths.BUTTER_DEALS_SVG,
        text = "Мои сделки",
        onClick = {},
    )

private val inbox =
    ButterModel(
        iconUrl = ImagePaths.BUTTER_MAIL_SVG,
        text = "Входящие",
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
    private val avatarRepository: AvatarRepository,
    private val carMenuViewModel: CarMenuViewModel,
    private val profileViewModel: ProfileViewModel? = null,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : ButterViewModel {
    private val _state = MutableStateFlow<ButterState>(ButterState.Idle)
    private var isMenuOpened = false

    override val state: StateFlow<ButterState>
        get() = _state.asStateFlow()

    init {
        carMenuViewModel.load()
        coroutineScope.launch {
            combine(
                carMenuViewModel.menuOption,
                carMenuViewModel.isLoading,
            ) { menuOption, isLoading ->
                if (isMenuOpened) {
                    updateMenu(menuOption, isLoading)
                }
            }.collect {}
        }
    }

    override fun open() {
        isMenuOpened = true
        if (carMenuViewModel.isLoading.value) {
            carMenuViewModel.load()
        }
        updateMenu(carMenuViewModel.menuOption.value, carMenuViewModel.isLoading.value)
    }

    private fun updateMenu(
        menuOption: CarMenuOption,
        isLoading: Boolean,
    ) {
        _state.value =
            ButterState.Opened(
                buildList {
                    if (storage.isLogined()) {
                        add(profile.copy(onClick = { close() }))
                        add(inbox.copy(onClick = { close() }))
                        add(myDocuments.copy(onClick = { close() }))
                        add(myBookings.copy(onClick = { close() }))
                        add(myDeals.copy(onClick = { close() }))
                    } else {
                        add(login.copy(onClick = { close() }))
                        add(registr.copy(onClick = { close() }))
                    }

                    if (storage.isLogined() && !isLoading) {
                        if (menuOption == CarMenuOption.MyCars) {
                            add(myCars.copy(onClick = { close() }))
                        } else {
                            add(beCameAHost.copy(onClick = { close() }))
                        }
                    }

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
        coroutineScope.launch {
            avatarRepository.refresh()
        }
        profileViewModel?.refresh()
    }

    override fun close() {
        isMenuOpened = false
        _state.value = ButterState.Idle
    }

    override fun onClick() {
        when (_state.value) {
            is ButterState.Idle -> open()
            is ButterState.Opened -> close()
        }
    }
}
