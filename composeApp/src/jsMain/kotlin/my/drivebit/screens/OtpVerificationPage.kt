package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import my.drivebit.components.ActionButton
import my.drivebit.components.PageWithLogo
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.network.services.Auth
import my.drivebit.shared.storage.Storage
import my.drivebit.utils.getUrlParameter
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.OtpVerificationState
import my.drivebit.viewmodels.OtpVerificationViewModel
import my.drivebit.viewmodels.createButtonViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

@Composable
fun OtpVerificationPage() {
    val navigationController = LocalNavigationController.current
    val auth: Auth = koinInject()
    val storage: Storage = koinInject()
    val identifier =
        remember {
            getUrlParameter("identifier")
        }

    val viewModel =
        remember(identifier) {
            OtpVerificationViewModel(
                auth = auth,
                storage = storage,
                identifier = identifier,
            )
        }

    val code by viewModel.code.collectAsState()
    val state by viewModel.state.collectAsState()
    val isLoading = state is OtpVerificationState.Loading
    val buttonViewModel = createButtonViewModel()

    LaunchedEffect(isLoading) {
        buttonViewModel.setState(if (isLoading) ButtonState.Loading else ButtonState.Enabled)
    }

    if (state is OtpVerificationState.Success) {
        navigationController?.navigateTo("/")
    }

    PageWithLogo {
        Div({
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.Center)
                minHeight(80.vh)
            }
        }) {
            Div({
                style {
                    width(100.percent)
                    maxWidth(400.px)
                }
            }) {
                Div({
                    style {
                        marginTop(0.px)
                    }
                }) {
                    Span({
                        style {
                            applyTypography(CSSTypography.Styles.body)
                            fontSize(CSSTypography.FontSize.xxl)
                            fontWeight(CSSTypography.FontWeight.bold)
                            color(CSSColors.Black)
                        }
                    }) {
                        Text("Введите код")
                    }
                }

                Div({
                    style {
                        marginTop(32.px)
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                        gap(16.px)
                    }
                }) {
                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Column)
                            gap(8.px)
                        }
                    }) {
                        Span({
                            style {
                                applyTypography(CSSTypography.Styles.body)
                                fontSize(CSSTypography.FontSize.sm)
                                fontWeight(CSSTypography.FontWeight.medium)
                                color(CSSColors.Gray600)
                            }
                        }) {
                            Text("Код подтверждения")
                        }
                        Input(type = org.jetbrains.compose.web.attributes.InputType.Text) {
                            value(code)
                            onInput { event ->
                                val inputValue = (event.target as org.w3c.dom.HTMLInputElement).value
                                viewModel.updateCode(inputValue)
                            }
                            style {
                                width(100.percent)
                                padding(12.px, 16.px)
                                borderRadius(8.px)
                                val borderColor =
                                    if (state is OtpVerificationState.Error) {
                                        CSSColors.RedString
                                    } else {
                                        CSSColors.Gray300String
                                    }
                                property("border", "1px solid $borderColor")
                                property("font-size", "20px")
                                property("letter-spacing", "8px")
                                property("text-align", "center")
                                property("outline", "none")
                                property("transition", "border-color 0.2s ease")
                                property("box-sizing", "border-box")
                            }
                            onFocus {
                                val borderColor =
                                    if (state is OtpVerificationState.Error) {
                                        CSSColors.RedString
                                    } else {
                                        CSSColors.BlueString
                                    }
                                (it.target as org.w3c.dom.HTMLInputElement).style.setProperty(
                                    "border-color",
                                    borderColor,
                                )
                            }
                            onBlur {
                                val borderColor =
                                    if (state is OtpVerificationState.Error) {
                                        CSSColors.RedString
                                    } else {
                                        CSSColors.Gray300String
                                    }
                                (it.target as org.w3c.dom.HTMLInputElement).style.setProperty(
                                    "border-color",
                                    borderColor,
                                )
                            }
                        }
                        if (state is OtpVerificationState.Error) {
                            Span({
                                style {
                                    applyTypography(CSSTypography.Styles.body)
                                    fontSize(CSSTypography.FontSize.sm)
                                    color(CSSColors.Red)
                                }
                            }) {
                                Text((state as OtpVerificationState.Error).message)
                            }
                        }
                    }

                    ActionButton(
                        viewModel = buttonViewModel,
                        enabledColor = CSSColors.Blue,
                        text = "Подтвердить",
                        onClick = {
                            viewModel.verifyOtp()
                        },
                    )
                }
            }
        }
    }
}
