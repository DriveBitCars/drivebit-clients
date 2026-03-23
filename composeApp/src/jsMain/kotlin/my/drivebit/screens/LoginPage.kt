package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import my.drivebit.components.TermsConsentCheckbox
import my.drivebit.components.TextSmallBodyBlack
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.navigation.NavigationState
import my.drivebit.resources.ImagePaths
import my.drivebit.utils.END_AT
import my.drivebit.utils.IDENTIFIER
import my.drivebit.utils.OTPRESULT
import my.drivebit.utils.OTP_RESULT_PARAM
import my.drivebit.utils.REDIRECT_PATH
import my.drivebit.utils.RETURN_CAR_ID
import my.drivebit.utils.START_AT
import my.drivebit.utils.encodeUrlParameter
import my.drivebit.utils.getUrlParameter
import my.drivebit.viewmodels.AuthFormState
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.ButtonViewModel
import my.drivebit.viewmodels.InputFieldType
import my.drivebit.viewmodels.ValidationState
import my.drivebit.viewmodels.login.LoginIntent
import my.drivebit.viewmodels.login.LoginMviViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.koin.compose.koinInject
import org.koin.core.qualifier.Qualifier

@Composable
fun LoginPage(mviQualifier: Qualifier) {
    val mvi: LoginMviViewModel = koinInject(mviQualifier)
    LoginPageContent(mvi = mvi)
}

@Composable
private fun LoginPageContent(mvi: LoginMviViewModel) {
    val navigationState: NavigationState = koinInject()
    val currentPath by navigationState.currentPath.collectAsState()
    val windowShowCycle by navigationState.windowShowRestoreCycle.collectAsState()
    val authForm = mvi.asAuthFormViewModel()
    val validatorViewModel = mvi.validatorViewModel
    val uiState by mvi.uiState.collectAsState()
    val inputValueState =
        remember {
            mutableStateOf(uiState.input)
        }

    LaunchedEffect(uiState.input) {
        inputValueState.value = uiState.input
    }

    val navigationController = LocalNavigationController.current!!
    val returnCarId = getUrlParameter(RETURN_CAR_ID)
    val startAt = getUrlParameter(START_AT)
    val endAt = getUrlParameter(END_AT)
    val redirectPath = getUrlParameter(REDIRECT_PATH)
    val loginState = uiState.authState
    val validationState by validatorViewModel.validationState.collectAsState()
    val termsConsentAccepted = uiState.termsAccepted

    var draftRestoreGeneration by remember { mutableIntStateOf(0) }
    val primaryButtonViewModel = remember { ButtonViewModel() }

    fun syncPrimaryButtonFromMvi() {
        val vs = validatorViewModel.validationState.value
        val u = mvi.uiState.value
        val isValid = vs is ValidationState.Valid
        val isLoading = u.authState is AuthFormState.Loading
        val consentOk = !u.requiresTermsConsent || u.termsAccepted
        when {
            isLoading -> primaryButtonViewModel.setState(ButtonState.Loading)
            !isValid -> primaryButtonViewModel.setState(ButtonState.Disabled)
            !consentOk -> primaryButtonViewModel.setState(ButtonState.Disabled)
            else -> primaryButtonViewModel.setState(ButtonState.Enabled)
        }
    }

    LaunchedEffect(currentPath) {
        if (currentPath.contains("login-by-phone") || currentPath.contains("login-by-mail")) {
            mvi.handleIntent(LoginIntent.RestoreDraft)
            syncPrimaryButtonFromMvi()
            draftRestoreGeneration++
        }
    }

    LaunchedEffect(windowShowCycle) {
        if (windowShowCycle == 0) return@LaunchedEffect
        if (currentPath.contains("login-by-phone") || currentPath.contains("login-by-mail")) {
            mvi.handleIntent(LoginIntent.RestoreDraft)
            syncPrimaryButtonFromMvi()
            draftRestoreGeneration++
        }
    }

    LaunchedEffect(loginState) {
        if (loginState is AuthFormState.Success) {
            mvi.clearDraftStorage()
        }
    }

    LaunchedEffect(
        draftRestoreGeneration,
        loginState,
        uiState.termsAccepted,
        uiState.requiresTermsConsent,
        uiState.input,
        validationState,
    ) {
        syncPrimaryButtonFromMvi()
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
        if (redirectPath.isNotBlank()) returnParams.add("$REDIRECT_PATH=${redirectPath.encodeUrlParameter()}")
        val returnParamsStr = returnParams.joinToString("&")
        val returnParamsFragment = if (returnParamsStr.isNotBlank()) "&$returnParamsStr" else ""
        navigationController.navigateTo(
            "/verify-otp?$IDENTIFIER=$encodedIdentifier&$OTP_RESULT_PARAM=$otpResult$returnParamsFragment",
        )
    }

    PageWithLogo {
        CenteredFormContainer {
            PageHeader {
                TextSmartHeader(uiState.pageTitle)
            }

            FormSection {
                InputField(
                    authFormViewModel = authForm,
                    validatorViewModel = validatorViewModel,
                    inputValue = inputValueState,
                    onFormattedValueCommitted = { formatted ->
                        mvi.handleIntent(LoginIntent.InputChanged(formatted))
                    },
                )
            }

            if (uiState.requiresTermsConsent) {
                TermsConsentCheckbox(
                    accepted = termsConsentAccepted,
                    onAcceptedChange = { mvi.handleIntent(LoginIntent.TermsChanged(it)) },
                )
            }

            ButtonContainer(id = "primary-login-button") {
                ActionButton(
                    viewModel = primaryButtonViewModel,
                    enabledColor = CSSColors.Blue,
                    text = uiState.primaryButtonText,
                    onClick = {
                        mvi.handleIntent(LoginIntent.Submit)
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
                    image = if (authForm.inputType == InputFieldType.Phone) ImagePaths.LOGIN_LETTER_SVG else null,
                    enabledColor = CSSColors.Gray300,
                    text = uiState.secondaryButtonText,
                    onClick = {
                        val basePath = uiState.secondaryButtonNavigationPath
                        val params = mutableListOf<String>()
                        if (returnCarId.isNotBlank()) params.add("$RETURN_CAR_ID=${returnCarId.encodeUrlParameter()}")
                        if (startAt.isNotBlank()) params.add("$START_AT=${startAt.encodeUrlParameter()}")
                        if (endAt.isNotBlank()) params.add("$END_AT=${endAt.encodeUrlParameter()}")
                        if (redirectPath.isNotBlank()) params.add("$REDIRECT_PATH=${redirectPath.encodeUrlParameter()}")
                        val path = if (params.isNotEmpty()) "$basePath?${params.joinToString("&")}" else basePath
                        navigationController.navigateTo(path)
                    },
                )
            }
        }
    }
}
