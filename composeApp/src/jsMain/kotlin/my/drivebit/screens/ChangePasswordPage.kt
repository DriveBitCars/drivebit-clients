package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import my.drivebit.components.ActionButton
import my.drivebit.components.ButtonContainer
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.FormSection
import my.drivebit.components.PageHeader
import my.drivebit.components.PageWithLogo
import my.drivebit.components.TextError
import my.drivebit.components.TextInputField
import my.drivebit.components.TextSmallBodyBlack
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.repositories.CreateOtpRepository
import my.drivebit.repositories.ResultOtp
import my.drivebit.utils.IDENTIFIER
import my.drivebit.utils.NEW_PASSWORD
import my.drivebit.utils.OTPRESULT
import my.drivebit.utils.OTP_RESULT_PARAM
import my.drivebit.utils.encodeUrlParameter
import my.drivebit.utils.getUrlParameter
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.createButtonViewModel
import org.jetbrains.compose.web.attributes.InputType
import org.koin.compose.koinInject

@Composable
fun ChangePasswordPage() {
    val createOtpRepository: CreateOtpRepository = koinInject()
    val navigationController = LocalNavigationController.current!!
    val login = getUrlParameter("login")

    var newPassword by remember { mutableStateOf("") }
    var repeatedPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val primaryButtonViewModel = createButtonViewModel()
    val isFormFilled = newPassword.isNotBlank() && repeatedPassword.isNotBlank()

    LaunchedEffect(isLoading, isFormFilled) {
        when {
            isLoading -> primaryButtonViewModel.setState(ButtonState.Loading)
            isFormFilled -> primaryButtonViewModel.setState(ButtonState.Enabled)
            else -> primaryButtonViewModel.setState(ButtonState.Disabled)
        }
    }

    fun validatePassword(password: String): String? {
        if (password.length < 8) return "Пароль должен быть не менее 8 символов"
        if (!password.any { it.isUpperCase() }) return "Добавьте хотя бы одну заглавную букву"
        if (!password.any { it.isLowerCase() }) return "Добавьте хотя бы одну строчную букву"
        if (!password.any { it.isDigit() }) return "Добавьте хотя бы одну цифру"
        if (password.none { !it.isLetterOrDigit() }) return "Добавьте хотя бы один спецсимвол"
        return null
    }

    PageWithLogo {
        CenteredFormContainer {
            PageHeader {
                TextSmartHeader("Смена пароля")
            }

            FormSection {
                TextSmallBodyBlack("Код подтверждения будет отправлен на текущий email/телефон профиля")

                TextInputField(
                    label = "Новый пароль",
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        errorMessage = null
                    },
                    inputType = InputType.Password,
                    fitContainerWidth = true,
                    placeholder = "Введите новый пароль",
                    passwordVisibilityToggle = true,
                )

                TextInputField(
                    label = "Повторите пароль",
                    value = repeatedPassword,
                    onValueChange = {
                        repeatedPassword = it
                        errorMessage = null
                    },
                    inputType = InputType.Password,
                    fitContainerWidth = true,
                    placeholder = "Повторите новый пароль",
                    passwordVisibilityToggle = true,
                )

                errorMessage?.let { TextError(it) }
            }

            ButtonContainer(id = "primary-change-password-button") {
                ActionButton(
                    viewModel = primaryButtonViewModel,
                    enabledColor = CSSColors.Blue,
                    text = "Отправить код",
                    onClick = {
                        if (login.isBlank()) {
                            errorMessage = "Не найден email/телефон для отправки кода"
                            return@ActionButton
                        }

                        val passwordValidationError = validatePassword(newPassword)
                        if (passwordValidationError != null) {
                            errorMessage = passwordValidationError
                            return@ActionButton
                        }
                        if (newPassword != repeatedPassword) {
                            errorMessage = "Пароли не совпадают"
                            return@ActionButton
                        }

                        isLoading = true
                        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
                            when (val result = createOtpRepository.createOtp(login)) {
                                is ResultOtp.Success -> {
                                    val identifier = result.sessionId.encodeUrlParameter()
                                    val encodedPassword = newPassword.encodeUrlParameter()
                                    val otpResult = OTPRESULT.ChangePassword.name
                                    navigationController.navigateTo(
                                        "/verify-otp?$IDENTIFIER=$identifier&$OTP_RESULT_PARAM=$otpResult&$NEW_PASSWORD=$encodedPassword",
                                    )
                                }
                                is ResultOtp.Error -> {
                                    errorMessage = result.message
                                    isLoading = false
                                }
                            }
                        }
                    },
                )
            }
        }
    }
}
