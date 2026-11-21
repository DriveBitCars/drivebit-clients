package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import kotlinx.browser.window
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.shared.storage.Storage
import my.drivebit.viewmodels.ButterEffect
import my.drivebit.viewmodels.ButterModel
import my.drivebit.viewmodels.ButterState
import my.drivebit.viewmodels.ButterViewModel
import my.drivebit.viewmodels.CarMenuViewModel
import my.drivebit.web.homePathHref
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.bottom
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.left
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.right
import org.jetbrains.compose.web.css.top
import org.jetbrains.compose.web.dom.Div
import org.koin.compose.koinInject

@Composable
@Suppress("FunctionName")
fun ButterMenu() {
    val butterViewModel: ButterViewModel = koinInject()
    val carMenuViewModel: CarMenuViewModel = koinInject()
    val storage: Storage = koinInject()
    val state = butterViewModel.state.collectAsState()
    val navigationController = LocalNavigationController.current

    LaunchedEffect(Unit) {
        carMenuViewModel.load()
    }

    LaunchedEffect(Unit) {
        butterViewModel.effects.collect { effect ->
            when (effect) {
                ButterEffect.NavigateToHome -> {
                    window.location.href = homePathHref(storage)
                }
            }
        }
    }

    when (val currentState = state.value) {
        is ButterState.Idle -> {
        }
        is ButterState.Opened -> {
            Div({
                style {
                    position(Position.Fixed)
                    top(0.px)
                    left(0.px)
                    right(0.px)
                    bottom(0.px)
                    property("z-index", "10050")
                }
                onClick {
                    butterViewModel.close()
                }
            }) {
                val isMobile = window.innerWidth <= 768
                // Mobile: omit Сообщения (bottom nav); keep Мои авто before Выйти
                val visibleItems =
                    if (isMobile) {
                        currentState.model.filter {
                            it.text != "Сообщения" && it.text != "Входящие"
                        }
                    } else {
                        currentState.model
                    }
                ButterItems(
                    modifier = Modifier,
                    items =
                        visibleItems.map { item ->
                            when (item.text) {
                                "Логин" ->
                                    item.copy(onClick = {
                                        butterViewModel.close()
                                        navigationController?.navigateTo("/login-by-phone")
                                    })
                                "Регистрация" ->
                                    item.copy(onClick = {
                                        butterViewModel.close()
                                        navigationController?.navigateTo("/login-by-phone")
                                    })
                                "Мой профиль" ->
                                    item.copy(onClick = {
                                        butterViewModel.close()
                                        navigationController?.navigateTo("/profile")
                                    })
                                "Мои авто" ->
                                    item.copy(onClick = {
                                        butterViewModel.close()
                                        navigationController?.navigateTo("/my-cars")
                                    })
                                "Сообщения", "Входящие" ->
                                    item.copy(onClick = {
                                        butterViewModel.close()
                                        navigationController?.navigateTo("/chats")
                                    })
                                "Мои документы" ->
                                    item.copy(onClick = {
                                        butterViewModel.close()
                                        navigationController?.navigateTo("/documents")
                                    })
                                "Мои бронирования" ->
                                    item.copy(onClick = {
                                        butterViewModel.close()
                                        navigationController?.navigateTo("/my-bookings")
                                    })
                                "Мои сделки" ->
                                    item.copy(onClick = {
                                        butterViewModel.close()
                                        navigationController?.navigateTo("/my-deals")
                                    })
                                else -> item
                            }
                        },
                )
            }
        }
    }
}

@Composable
@Suppress("FunctionName")
fun ButterItems(
    modifier: Modifier,
    items: List<ButterModel>,
) {
    Div({
        classes("butter-menu-container", "drivebit-butter-menu")
        onClick { event ->
            event.stopPropagation()
        }
    }) {
        items.forEach { item ->
            Div({
                onClick { event ->
                    item.onClick()
                    event.stopPropagation()
                }
                style {
                    cursor("pointer")
                    property("transition", "background-color 0.2s ease")
                    borderRadius(8.px)
                    padding(12.px, 8.px)
                }
                classes("butter-menu-item")
            }) {
                Item(
                    icon = item.iconUrl,
                    text = item.text,
                    showDivider = false,
                )
            }
        }
    }
}
