package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import my.drivebit.components.ActionButton
import my.drivebit.components.InputField
import my.drivebit.components.PageWithLogo
import my.drivebit.components.TextSmallBodyBlack
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.resources.ImagePaths
import my.drivebit.utils.IDENTIFIER
import my.drivebit.utils.OTPRESULT
import my.drivebit.utils.OTP_RESULT_PARAM
import my.drivebit.utils.encodeUrlParameter
import my.drivebit.viewmodels.AuthFormState
import my.drivebit.viewmodels.AuthFormViewModel
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.InputFieldType
import my.drivebit.viewmodels.ValidationState
import my.drivebit.viewmodels.ValidatorViewModel
import my.drivebit.viewmodels.createButtonViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.koin.compose.koinInject
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.named

@Composable
fun LoginPage(viewModelQualifier: Qualifier) {
    val viewModel: AuthFormViewModel = koinInject(viewModelQualifier)
    val validatorViewModel: ValidatorViewModel =
        koinInject(
            if (viewModel.inputType == InputFieldType.Phone) {
                named("phoneInputField")
            } else {
                named("emailInputField")
            },
        )

    LoginPageContent(
        viewModel = viewModel,
        validatorViewModel = validatorViewModel,
    )
}

@Composable
private fun LoginPageContent(
    viewModel: AuthFormViewModel,
    validatorViewModel: ValidatorViewModel,
) {
    val inputValueState =
        remember {
            mutableStateOf(viewModel.initialInputValue)
        }
    var inputValue by inputValueState
    val navigationController = LocalNavigationController.current!!
    val loginState by viewModel.state.collectAsState()
    val validationState by validatorViewModel.validationState.collectAsState()

    val isValid = derivedStateOf { validationState is ValidationState.Valid }
    val isLoading = derivedStateOf { loginState is AuthFormState.Loading }

    val primaryButtonViewModel = createButtonViewModel()

    LaunchedEffect(isLoading.value, isValid.value) {
        when {
            isLoading.value -> primaryButtonViewModel.setState(ButtonState.Loading)
            !isValid.value -> primaryButtonViewModel.setState(ButtonState.Disabled)
            else -> primaryButtonViewModel.setState(ButtonState.Enabled)
        }
    }

    if (loginState is AuthFormState.Error) {
        val errorMessage = (loginState as AuthFormState.Error).message
        validatorViewModel.setError(errorMessage)
    }

    if (loginState is AuthFormState.Success) {
        val identifier = (loginState as AuthFormState.Success).identifier
        val encodedIdentifier = identifier.encodeUrlParameter()
        val otpResult = OTPRESULT.VerifyOtp.name
        navigationController.navigateTo("/verify-otp?$IDENTIFIER=$encodedIdentifier&$OTP_RESULT_PARAM=$otpResult")
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
                    TextSmartHeader(viewModel.pageTitle)
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
                        authFormViewModel = viewModel,
                        validatorViewModel = validatorViewModel,
                        inputValue = inputValueState,
                    )
                }

                Div({
                    style {
                        property("id", "primary-login-button")
                        marginTop(24.px)
                    }
                }) {
                    ActionButton(
                        viewModel = primaryButtonViewModel,
                        enabledColor = CSSColors.Blue,
                        text = viewModel.primaryButtonText,
                        onClick = {
                            viewModel.submit(inputValue)
                        },
                    )
                }

                Div({
                    style {
                        marginTop(16.px)
                        textAlign("center")
                    }
                }) {
                    TextSmallBodyBlack("Или")
                }

                Div({
                    style {
                        marginTop(16.px)
                    }
                }) {
                    ActionButton(
                        image = if (viewModel.inputType == InputFieldType.Phone) ImagePaths.LOGIN_LETTER_SVG else null,
                        enabledColor = CSSColors.Gray300,
                        text = viewModel.secondaryButtonText,
                        onClick = {
                            val path = viewModel.secondaryButtonNavigationPath
                            navigationController.navigateTo(path)
                        },
                    )
                }
            }
        }
    }
}
