package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.browser.window
import my.drivebit.components.ActionButton
import my.drivebit.components.ButtonContainer
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.FormSection
import my.drivebit.components.PageHeader
import my.drivebit.components.PageWithLogo
import my.drivebit.components.TermsConsentCheckbox
import my.drivebit.components.TextError
import my.drivebit.components.TextInputField
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.utils.END_AT
import my.drivebit.utils.REDIRECT_PATH
import my.drivebit.utils.RETURN_CAR_ID
import my.drivebit.utils.START_AT
import my.drivebit.utils.encodeUrlParameter
import my.drivebit.utils.getUrlParameter
import my.drivebit.shared.storage.Storage
import my.drivebit.web.homePathHref
import my.drivebit.viewmodels.AuthFormState
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.PasswordLoginViewModel
import my.drivebit.viewmodels.createButtonViewModel
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.koin.compose.koinInject

@Composable
fun LoginByPasswordPage() {
    val storage: Storage = koinInject()
    val viewModel: PasswordLoginViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val returnCarId = getUrlParameter(RETURN_CAR_ID)
    val startAt = getUrlParameter(START_AT)
    val endAt = getUrlParameter(END_AT)
    val redirectPath = getUrlParameter(REDIRECT_PATH)

    val buttonViewModel = createButtonViewModel()
    val isLoading = uiState.authState is AuthFormState.Loading
    val isEnabled =
        uiState.login.trim().isNotEmpty() &&
            uiState.password.isNotEmpty() &&
            uiState.termsAccepted &&
            !isLoading

    LaunchedEffect(isLoading, isEnabled) {
        when {
            isLoading -> buttonViewModel.setState(ButtonState.Loading)
            isEnabled -> buttonViewModel.setState(ButtonState.Enabled)
            else -> buttonViewModel.setState(ButtonState.Disabled)
        }
    }

    LaunchedEffect(uiState.authState) {
        if (uiState.authState is AuthFormState.Success) {
            val targetPath =
                when {
                    redirectPath.isNotBlank() -> redirectPath
                    returnCarId.isNotBlank() -> {
                        val params = mutableListOf("id=${returnCarId.encodeUrlParameter()}")
                        if (startAt.isNotBlank()) params.add("$START_AT=${startAt.encodeUrlParameter()}")
                        if (endAt.isNotBlank()) params.add("$END_AT=${endAt.encodeUrlParameter()}")
                        "/car-detail?${params.joinToString("&")}"
                    }
                    else -> homePathHref(storage)
                }
            window.location.href = targetPath
        }
    }

    PageWithLogo {
        CenteredFormContainer {
            PageHeader {
                TextSmartHeader("Вход по логину и паролю")
            }

            FormSection {
                TextInputField(
                    label = "Логин",
                    value = uiState.login,
                    fitContainerWidth = true,
                    placeholder = "Email или телефон",
                    onValueChange = viewModel::onLoginChanged,
                )

                PasswordInputField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChanged,
                )

                TermsConsentCheckbox(
                    accepted = uiState.termsAccepted,
                    onAcceptedChange = viewModel::onTermsChanged,
                )

                if (uiState.authState is AuthFormState.Error) {
                    TextError((uiState.authState as AuthFormState.Error).message)
                }

                ButtonContainer(id = "primary-password-login-button") {
                    Div({
                        style { width(100.percent) }
                    }) {
                        ActionButton(
                            viewModel = buttonViewModel,
                            enabledColor = CSSColors.Blue,
                            text = "Войти",
                            onClick = viewModel::submit,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordInputField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    TextInputField(
        label = "Пароль",
        value = value,
        fitContainerWidth = true,
        inputType = InputType.Password,
        placeholder = "Введите пароль",
        passwordVisibilityToggle = true,
        onValueChange = onValueChange,
    )
}
