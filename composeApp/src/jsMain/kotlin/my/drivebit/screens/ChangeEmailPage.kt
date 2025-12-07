package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import my.drivebit.components.ActionButton
import my.drivebit.components.InputField
import my.drivebit.components.PageWithLogo
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.utils.IDENTIFIER
import my.drivebit.utils.NEW_LOGIN
import my.drivebit.utils.OTPRESULT
import my.drivebit.utils.OTP_RESULT_PARAM
import my.drivebit.utils.encodeUrlParameter
import my.drivebit.viewmodels.AuthFormViewModel
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.CreateOtpRepository
import my.drivebit.viewmodels.ResultOtp
import my.drivebit.viewmodels.ValidationState
import my.drivebit.viewmodels.ValidatorViewModel
import my.drivebit.viewmodels.createButtonViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun ChangeEmailPage() {
    val createOtpRepository: CreateOtpRepository = koinInject()
    val validatorViewModel: ValidatorViewModel = koinInject(named("emailInputField"))
    val authFormViewModel: AuthFormViewModel = koinInject(named("email"))

    ChangeEmailPageContent(
        createOtpRepository = createOtpRepository,
        validatorViewModel = validatorViewModel,
        authFormViewModel = authFormViewModel,
    )
}

@Composable
private fun ChangeEmailPageContent(
    createOtpRepository: CreateOtpRepository,
    validatorViewModel: ValidatorViewModel,
    authFormViewModel: AuthFormViewModel,
) {
    val inputValueState =
        remember {
            mutableStateOf(authFormViewModel.initialInputValue)
        }
    var inputValue by inputValueState
    val navigationController = LocalNavigationController.current!!
    val validationState by validatorViewModel.validationState.collectAsState()

    val isValid = derivedStateOf { validationState is ValidationState.Valid }
    val isLoading = remember { mutableStateOf(false) }

    val primaryButtonViewModel = createButtonViewModel()

    LaunchedEffect(isLoading.value, isValid.value) {
        when {
            isLoading.value -> primaryButtonViewModel.setState(ButtonState.Loading)
            !isValid.value -> primaryButtonViewModel.setState(ButtonState.Disabled)
            else -> primaryButtonViewModel.setState(ButtonState.Enabled)
        }
    }

    val createOtpState = remember { mutableStateOf<ResultOtp?>(null) }

    LaunchedEffect(createOtpState.value) {
        val result = createOtpState.value
        if (result is ResultOtp.Success) {
            val identifier = result.sessionId
            val encodedIdentifier = identifier.encodeUrlParameter()
            val encodedEmail = inputValue.encodeUrlParameter()
            val otpResult = OTPRESULT.ChangeEmail.name
            navigationController.navigateTo("/verify-otp?$IDENTIFIER=$encodedIdentifier&$OTP_RESULT_PARAM=$otpResult&$NEW_LOGIN=$encodedEmail")
        } else if (result is ResultOtp.Error) {
            validatorViewModel.setError(result.message)
            isLoading.value = false
        }
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
                        Text("Смена email")
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
                    InputField(
                        authFormViewModel = authFormViewModel,
                        validatorViewModel = validatorViewModel,
                        inputValue = inputValueState,
                    )
                }

                Div({
                    style {
                        property("id", "primary-change-email-button")
                        marginTop(24.px)
                    }
                }) {
                    ActionButton(
                        viewModel = primaryButtonViewModel,
                        enabledColor = CSSColors.Blue,
                        text = "Отправить код",
                        onClick = {
                            validatorViewModel.validateInput(inputValue)
                            if (isValid.value) {
                                isLoading.value = true
                                CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
                                    createOtpState.value = createOtpRepository.createOtp(inputValue)
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}
