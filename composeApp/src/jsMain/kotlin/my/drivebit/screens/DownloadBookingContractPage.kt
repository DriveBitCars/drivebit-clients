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
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.shared.storage.Storage
import my.drivebit.utils.REDIRECT_PATH
import my.drivebit.utils.encodeUrlParameter
import my.drivebit.utils.getUrlParameter
import my.drivebit.utils.mapIso8601ToDateString
import my.drivebit.utils.mapIso8601ToTimeString
import my.drivebit.viewmodels.BookingContractUiState
import my.drivebit.viewmodels.BookingContractViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.currentKoinScope
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun DownloadBookingContractPage() {
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
    val viewModel: BookingContractViewModel =
        remember(bookingId) {
            koinScope.get(parameters = { parametersOf(bookingId) })
        }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(bookingId) {
        viewModel.loadContract()
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
                BookingContractUiState.Idle,
                BookingContractUiState.Loading,
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
                            Text("Загружаем договор…")
                        }
                    }
                }
                is BookingContractUiState.Ready -> {
                    val contract = state.contract
                    val expiresAt =
                        runCatching {
                            "${mapIso8601ToDateString(contract.urlExpiresAt)} ${mapIso8601ToTimeString(contract.urlExpiresAt)}"
                        }.getOrElse { contract.urlExpiresAt }
                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Column)
                            property("gap", "16px")
                        }
                    }) {
                        TextSmartHeader("Договор аренды №${contract.contractNumber}")
                        P({
                            style {
                                applyTypography(CSSTypography.Styles.body)
                                color(CSSColors.Gray600)
                                property("margin", "0")
                            }
                        }) {
                            Text("Файл: ${contract.fileName}")
                        }
                        P({
                            style {
                                applyTypography(CSSTypography.Styles.body)
                                color(CSSColors.Gray600)
                                property("margin", "0")
                            }
                        }) {
                            Text("Ссылка действует до: $expiresAt")
                        }
                        Button({
                            style {
                                padding(12.px, 24.px)
                                backgroundColor(CSSColors.Blue)
                                color(CSSColors.White)
                                border(0.px)
                                borderRadius(8.px)
                                fontSize(16.px)
                                fontWeight("600")
                                cursor("pointer")
                                property("align-self", "flex-start")
                            }
                            onClick {
                                window.open(contract.downloadUrl, "_blank")
                            }
                        }) {
                            Text("Скачать договор")
                        }
                    }
                }
                is BookingContractUiState.Error -> {
                    TextError(state.message)
                }
            }
        }
    }
}
