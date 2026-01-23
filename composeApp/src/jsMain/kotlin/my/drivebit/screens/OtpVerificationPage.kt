package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import my.drivebit.components.ActionButton
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.FormSection
import my.drivebit.components.PageHeader
import my.drivebit.components.PageWithLogo
import my.drivebit.components.TextError
import my.drivebit.components.TextSmallBodyBlack
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.repositories.OtpResultRepository
import my.drivebit.utils.IDENTIFIER
import my.drivebit.utils.NEW_LOGIN
import my.drivebit.utils.OTPRESULT
import my.drivebit.utils.OTP_RESULT_PARAM
import my.drivebit.utils.getUrlParameter
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.OtpVerificationState
import my.drivebit.viewmodels.OtpVerificationViewModel
import my.drivebit.viewmodels.createButtonViewModel
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import org.w3c.dom.HTMLInputElement

@Composable
fun OtpVerificationPage() {
    val navigationController = LocalNavigationController.current
    val otpResultParam = getUrlParameter(OTP_RESULT_PARAM)
    val otpResultType =
        remember(otpResultParam) {
            OTPRESULT.fromString(otpResultParam)!!
        }
    val otpResultRepository: OtpResultRepository =
        koinInject(named(otpResultType.name))
    val identifier = getUrlParameter(IDENTIFIER)
    val newLogin = getUrlParameter(NEW_LOGIN)
    val additionalParams =
        remember(newLogin) {
            if (newLogin.isNotEmpty()) {
                mapOf("newLogin" to newLogin)
            } else {
                emptyMap()
            }
        }

    val viewModel =
        remember(identifier, otpResultRepository, additionalParams) {
            OtpVerificationViewModel(
                otpResultRepository = otpResultRepository,
                identifier = identifier,
                additionalParams = additionalParams,
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
        LaunchedEffect(Unit) {
            when (otpResultType) {
                OTPRESULT.VerifyOtp -> navigationController?.navigateTo("/")
                OTPRESULT.ChangeEmail -> navigationController?.navigateTo("/profile")
                OTPRESULT.ChangePhone -> navigationController?.navigateTo("/profile")
            }
        }
    }

    PageWithLogo {
        CenteredFormContainer {
            PageHeader {
                TextSmartHeader("Введите код")
            }

            FormSection {
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                        gap(8.px)
                    }
                }) {
                    TextSmallBodyBlack("Код подтверждения")

                    Input(type = InputType.Text) {
                        value(code)
                        onInput { event ->
                            val inputValue = (event.target as HTMLInputElement).value
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
                            (it.target as HTMLInputElement).style.setProperty(
                                "border-color",
                                borderColor,
                            )
                        }
                    }
                    if (state is OtpVerificationState.Error) {
                        TextError((state as OtpVerificationState.Error).message)
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
