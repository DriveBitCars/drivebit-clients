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
import my.drivebit.components.ButtonContainer
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.FormSection
import my.drivebit.components.InputField
import my.drivebit.components.PageHeader
import my.drivebit.components.PageWithLogo
import my.drivebit.components.TextSmallBodyBlack
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.resources.ImagePaths
import my.drivebit.utils.END_AT
import my.drivebit.utils.IDENTIFIER
import my.drivebit.utils.OTPRESULT
import my.drivebit.utils.OTP_RESULT_PARAM
import my.drivebit.utils.RETURN_CAR_ID
import my.drivebit.utils.START_AT
import my.drivebit.utils.encodeUrlParameter
import my.drivebit.utils.getUrlParameter
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
    val returnCarId = getUrlParameter(RETURN_CAR_ID)
    val startAt = getUrlParameter(START_AT)
    val endAt = getUrlParameter(END_AT)
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
        val returnParams = mutableListOf<String>()
        if (returnCarId.isNotBlank()) returnParams.add("$RETURN_CAR_ID=${returnCarId.encodeUrlParameter()}")
        if (startAt.isNotBlank()) returnParams.add("$START_AT=${startAt.encodeUrlParameter()}")
        if (endAt.isNotBlank()) returnParams.add("$END_AT=${endAt.encodeUrlParameter()}")
        val returnParamsStr = returnParams.joinToString("&")
        val returnParamsFragment = if (returnParamsStr.isNotBlank()) "&$returnParamsStr" else ""
        navigationController.navigateTo(
            "/verify-otp?$IDENTIFIER=$encodedIdentifier&$OTP_RESULT_PARAM=$otpResult$returnParamsFragment",
        )
    }

    PageWithLogo {
        CenteredFormContainer {
            PageHeader {
                TextSmartHeader(viewModel.pageTitle)
            }

            FormSection {
                InputField(
                    authFormViewModel = viewModel,
                    validatorViewModel = validatorViewModel,
                    inputValue = inputValueState,
                )
            }

            ButtonContainer(id = "primary-login-button") {
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

            ButtonContainer(marginTop = 16.px) {
                ActionButton(
                    image = if (viewModel.inputType == InputFieldType.Phone) ImagePaths.LOGIN_LETTER_SVG else null,
                    enabledColor = CSSColors.Gray300,
                    text = viewModel.secondaryButtonText,
                    onClick = {
                        val basePath = viewModel.secondaryButtonNavigationPath
                        val params = mutableListOf<String>()
                        if (returnCarId.isNotBlank()) params.add("$RETURN_CAR_ID=${returnCarId.encodeUrlParameter()}")
                        if (startAt.isNotBlank()) params.add("$START_AT=${startAt.encodeUrlParameter()}")
                        if (endAt.isNotBlank()) params.add("$END_AT=${endAt.encodeUrlParameter()}")
                        val path = if (params.isNotEmpty()) "$basePath?${params.joinToString("&")}" else basePath
                        navigationController.navigateTo(path)
                    },
                )
            }
        }
    }
}
