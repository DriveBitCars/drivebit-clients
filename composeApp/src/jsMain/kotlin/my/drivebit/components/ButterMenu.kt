package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import my.drivebit.design.CSSColors
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.viewmodels.ButterModel
import my.drivebit.viewmodels.ButterState
import my.drivebit.viewmodels.ButterViewModel
import my.drivebit.viewmodels.CarMenuViewModel
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.bottom
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.left
import org.jetbrains.compose.web.css.maxHeight
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.right
import org.jetbrains.compose.web.css.top
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.koin.compose.koinInject

@Composable
@Suppress("FunctionName")
fun ButterMenu() {
    val butterViewModel: ButterViewModel = koinInject()
    val carMenuViewModel: CarMenuViewModel = koinInject()
    val state = butterViewModel.state.collectAsState()
    val navigationController = LocalNavigationController.current

    LaunchedEffect(Unit) {
        carMenuViewModel.load()
    }

    when (val currentState = state.value) {
        is ButterState.Idle -> {
        }
        is ButterState.Opened -> {
            Div({
                style {
                    position(Position.Absolute)
                    top(0.px)
                    left(0.px)
                    right(0.px)
                    bottom(0.px)
                    property("z-index", "999")
                }
                onClick {
                    butterViewModel.close()
                }
            }) {
                ButterItems(
                    modifier = Modifier,
                    items =
                        currentState.model.map { item ->
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
                                "Сдать авто" ->
                                    item.copy(onClick = {
                                        butterViewModel.close()
                                        navigationController?.navigateTo("/list-your-car")
                                    })
                                "Мои авто" ->
                                    item.copy(onClick = {
                                        butterViewModel.close()
                                        navigationController?.navigateTo("/my-cars")
                                    })
                                "Входящие" ->
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
        style {
            position(Position.Absolute)
            top(72.px)
            right(36.px)
            width(280.px)
            backgroundColor(CSSColors.White)
            borderRadius(8.px)
            property("box-shadow", "0 4px 12px rgba(0, 0, 0, 0.15)")
            property("z-index", "1000")
            maxHeight(80.vh)
            property("overflow-y", "auto")
            property("overflow-x", "hidden")
            padding(8.px)
        }
        classes("butter-menu-container")
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
