package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.browser.window
import my.drivebit.analytics.reachYandexGoalArenda
import my.drivebit.components.ActionButton
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.Column
import my.drivebit.components.FormSection
import my.drivebit.components.PageHeader
import my.drivebit.components.PageWithLogo
import my.drivebit.components.TextError
import my.drivebit.components.TextSmallBodyBlack
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.repositories.OtpResultRepository
import my.drivebit.shared.storage.Storage
import my.drivebit.web.homePathHref
import my.drivebit.utils.AUTO_BOOK_AFTER_LOGIN
import my.drivebit.utils.END_AT
import my.drivebit.utils.IDENTIFIER
import my.drivebit.utils.NEW_LOGIN
import my.drivebit.utils.NEW_PASSWORD
import my.drivebit.utils.OTPRESULT
import my.drivebit.utils.OTP_RESULT_PARAM
import my.drivebit.utils.REDIRECT_PATH
import my.drivebit.utils.RETURN_CAR_ID
import my.drivebit.utils.START_AT
import my.drivebit.utils.encodeUrlParameter
import my.drivebit.utils.getUrlParameter
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.OtpVerificationState
import my.drivebit.viewmodels.OtpVerificationViewModel
import my.drivebit.viewmodels.createButtonViewModel
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Input
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import org.w3c.dom.HTMLInputElement

@Composable
fun OtpVerificationPage() {
    val storage: Storage = koinInject()
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
    val newPassword = getUrlParameter(NEW_PASSWORD)
    val returnCarId = getUrlParameter(RETURN_CAR_ID)
    val startAt = getUrlParameter(START_AT)
    val endAt = getUrlParameter(END_AT)
    val redirectPath = getUrlParameter(REDIRECT_PATH)
    val additionalParams =
        remember(newLogin, newPassword) {
            buildMap {
                if (newLogin.isNotEmpty()) put("newLogin", newLogin)
                if (newPassword.isNotEmpty()) put("newPassword", newPassword)
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
                OTPRESULT.VerifyOtp -> {
                    val targetPath =
                        when {
                            redirectPath.isNotBlank() -> redirectPath
                            returnCarId.isNotBlank() -> {
                                val params = mutableListOf("id=${returnCarId.encodeUrlParameter()}")
                                if (startAt.isNotBlank()) params.add("$START_AT=${startAt.encodeUrlParameter()}")
                                if (endAt.isNotBlank()) params.add("$END_AT=${endAt.encodeUrlParameter()}")
                                params.add("$AUTO_BOOK_AFTER_LOGIN=1")
                                "/car-detail?${params.joinToString("&")}"
                            }
                            else -> homePathHref(storage)
                        }
                    window.location.href = targetPath
                }
                OTPRESULT.ChangeEmail -> navigationController?.navigateTo("/profile")
                OTPRESULT.ChangePhone -> navigationController?.navigateTo("/profile")
                OTPRESULT.ChangePassword -> navigationController?.navigateTo("/profile")
            }
        }
    }

    PageWithLogo {
        CenteredFormContainer {
            PageHeader {
                TextSmartHeader("Введите код")
            }

            FormSection {
                Column(gap = 8.px) {
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
                        reachYandexGoalArenda()
                        viewModel.verifyOtp()
                    },
                )
            }
        }
    }
}
