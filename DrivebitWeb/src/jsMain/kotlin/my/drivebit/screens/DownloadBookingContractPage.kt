package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.browser.window
import my.drivebit.components.ActionButton
import my.drivebit.shell.AppWithHeader
import my.drivebit.components.Column
import my.drivebit.components.Loader
import my.drivebit.components.TextError
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.shared.storage.Storage
import my.drivebit.utils.REDIRECT_PATH
import my.drivebit.utils.encodeUrlParameter
import my.drivebit.utils.getUrlParameter
import my.drivebit.utils.mapIso8601ToDateString
import my.drivebit.utils.mapIso8601ToTimeString
import my.drivebit.utils.minioProxiedAbsoluteUrl
import my.drivebit.viewmodels.BookingContractUiState
import my.drivebit.viewmodels.BookingContractViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.Ul
import org.koin.compose.currentKoinScope
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun DownloadBookingContractPage() {
    val storage: Storage = koinInject()
    val navigationController = LocalNavigationController.current
    val bookingId = getUrlParameter("bookingId").trim()

    if (bookingId.isBlank()) {
        AppWithHeader {
            ContractPageContainer {
                ContractErrorContent(
                    message = "В ссылке не указан номер бронирования (параметр bookingId).",
                    onBack = { navigateBackFromContract(navigationController) },
                )
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
        ContractPageContainer {
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
                    val downloadUrl = minioProxiedAbsoluteUrl(contract.downloadUrl)
                    val expiresAt =
                        runCatching {
                            "${mapIso8601ToDateString(contract.urlExpiresAt)} ${mapIso8601ToTimeString(contract.urlExpiresAt)}"
                        }.getOrElse { contract.urlExpiresAt }
                    LaunchedEffect(downloadUrl) {
                        window.location.href = downloadUrl
                    }
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
                                window.location.href = downloadUrl
                            }
                        }) {
                            Text("Скачать договор")
                        }
                    }
                }
                is BookingContractUiState.Error ->
                    ContractErrorContent(
                        message = state.message,
                        reasons = state.reasons,
                        onBack = { navigateBackFromContract(navigationController) },
                    )
            }
        }
    }
}

@Composable
private fun ContractPageContainer(content: @Composable () -> Unit) {
    Div({
        style {
            padding(24.px)
            property("max-width", "560px")
            property("margin", "0 auto")
        }
    }) {
        content()
    }
}

@Composable
private fun ContractErrorContent(
    message: String,
    reasons: List<String> = emptyList(),
    onBack: () -> Unit,
) {
    Column(gap = 16.px) {
        TextError(message)
        if (reasons.isNotEmpty()) {
            P({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    color(CSSColors.Gray600)
                    fontWeight(CSSTypography.FontWeight.semibold)
                    property("margin", "0")
                }
            }) {
                Text("Что нужно заполнить:")
            }
            Ul({
                style {
                    property("margin", "0")
                    paddingLeft(20.px)
                    color(CSSColors.Gray600)
                    applyTypography(CSSTypography.Styles.body)
                    lineHeight("1.6")
                }
            }) {
                reasons.forEach { reason ->
                    Li {
                        Text(reason)
                    }
                }
            }
        }
        Div({
            style {
                width(100.percent)
                maxWidth(280.px)
                marginTop(8.px)
            }
        }) {
            ActionButton(
                enabledColor = CSSColors.Blue,
                text = "Назад",
                onClick = onBack,
            )
        }
    }
}

private fun navigateBackFromContract(navigationController: my.drivebit.navigation.NavigationController?) {
    if (navigationController != null) {
        navigationController.goBack()
    } else {
        window.location.href = "/my-bookings"
    }
}
