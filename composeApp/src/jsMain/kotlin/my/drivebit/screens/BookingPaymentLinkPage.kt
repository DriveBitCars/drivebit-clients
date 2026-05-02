package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.browser.window
import my.drivebit.components.AppWithHeader
import my.drivebit.components.Loader
import my.drivebit.components.TextError
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.shared.storage.Storage
import my.drivebit.utils.REDIRECT_PATH
import my.drivebit.utils.encodeUrlParameter
import my.drivebit.utils.getUrlParameter
import my.drivebit.viewmodels.BookingPaymentLinkUiState
import my.drivebit.viewmodels.BookingPaymentLinkViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.currentKoinScope
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun BookingPaymentLinkPage() {
    val storage: Storage = koinInject()
    val bookingId = getUrlParameter("bookingId").trim()

    if (bookingId.isBlank()) {
        AppWithHeader {
            Div({
                style {
                    padding(24.px)
                    property("max-width", "560px")
                    property("margin", "0 auto")
                }
            }) {
                TextError("В ссылке не указан номер бронирования (параметр bookingId).")
            }
        }
        return
    }

    if (!storage.isLogined()) {
        LaunchedEffect(bookingId) {
            val returnTo = window.location.pathname + window.location.search
            window.location.href = "/login-by-phone?$REDIRECT_PATH=${returnTo.encodeUrlParameter()}"
        }
        AppWithHeader {
            Div({
                style {
                    padding(48.px)
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    alignItems(AlignItems.Center)
                    property("gap", "16px")
                }
            }) {
                Loader()
                P({
                    style {
                        applyTypography(CSSTypography.Styles.body)
                        color(CSSColors.Gray600)
                        property("text-align", "center")
                        property("margin", "0")
                    }
                }) {
                    Text("Перенаправляем на вход…")
                }
            }
        }
        return
    }

    val koinScope = currentKoinScope()
    val viewModel: BookingPaymentLinkViewModel =
        remember(bookingId) {
            koinScope.get(parameters = { parametersOf(bookingId) })
        }
    val uiState by viewModel.uiState.collectAsState()

    val origin = window.location.origin.trimEnd('/')
    LaunchedEffect(bookingId) {
        viewModel.startCheckout(
            returnUrl = "$origin/payment-success",
            failUrl = "$origin/payment-failure",
        )
    }

    val checkoutUrl = (uiState as? BookingPaymentLinkUiState.OpenCheckout)?.url
    LaunchedEffect(checkoutUrl) {
        val url = checkoutUrl ?: return@LaunchedEffect
        window.location.href = url
    }

    AppWithHeader {
        Div({
            style {
                padding(24.px)
                property("max-width", "560px")
                property("margin", "0 auto")
            }
        }) {
            when (val state = uiState) {
                BookingPaymentLinkUiState.Idle,
                BookingPaymentLinkUiState.Loading,
                -> {
                    Div({
                        style {
                            padding(32.px)
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Column)
                            alignItems(AlignItems.Center)
                            property("gap", "16px")
                        }
                    }) {
                        Loader()
                        P({
                            style {
                                applyTypography(CSSTypography.Styles.body)
                                color(CSSColors.Gray600)
                                property("text-align", "center")
                                property("margin", "0")
                            }
                        }) {
                            Text("Подготавливаем оплату…")
                        }
                    }
                }
                is BookingPaymentLinkUiState.OpenCheckout -> {
                    Div({
                        style {
                            padding(32.px)
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Column)
                            alignItems(AlignItems.Center)
                        }
                    }) {
                        Loader()
                        P({
                            style {
                                applyTypography(CSSTypography.Styles.body)
                                marginTop(12.px)
                                color(CSSColors.Gray600)
                                property("text-align", "center")
                                property("margin", "0")
                            }
                        }) {
                            Text("Переход к оплате…")
                        }
                    }
                }
                is BookingPaymentLinkUiState.FinishedWithMessage -> {
                    P({
                        style {
                            applyTypography(CSSTypography.Styles.body)
                            color(CSSColors.Black)
                            property("text-align", "center")
                            property("margin", "0")
                        }
                    }) {
                        Text(state.text)
                    }
                }
            }
        }
    }
}
