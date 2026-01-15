package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import my.drivebit.components.ActionButton
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.FormSection
import my.drivebit.components.PageHeader
import my.drivebit.components.PageWithLogo
import my.drivebit.components.TextError
import my.drivebit.components.TextInputField
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.repositories.CarDataRepository
import my.drivebit.utils.encodeUrlParameter
import my.drivebit.utils.getUrlParameter
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.CreateCarFromDailyRateState
import my.drivebit.viewmodels.CreateCarFromDailyRateViewModel
import my.drivebit.viewmodels.createButtonViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.koin.compose.koinInject

@Composable
fun DailyRateInputPage(onDailyRateEntered: () -> Unit = {}) {
    val carDataRepository: CarDataRepository = koinInject()
    val createCarViewModel: CreateCarFromDailyRateViewModel = koinInject()
    val createCarState by createCarViewModel.state.collectAsState()
    val buttonViewModel = createButtonViewModel()

    var dailyRate by remember {
        val savedRate = carDataRepository.getDailyRate()
        mutableStateOf(savedRate?.toString() ?: "")
    }
    var localError by remember { mutableStateOf<String?>(null) }
    val autoCreateProcessed = remember { mutableStateOf(false) }

    val isLoading = createCarState is CreateCarFromDailyRateState.Loading
    val error = localError ?: (createCarState as? CreateCarFromDailyRateState.Error)?.message

    val isValid = dailyRate.toDoubleOrNull()?.let { it >= 0 } == true && !isLoading

    buttonViewModel.setState(
        if (isValid) ButtonState.Enabled else ButtonState.Disabled,
    )

    LaunchedEffect(createCarState) {
        when (createCarState) {
            CreateCarFromDailyRateState.MissingPassport -> {
                val after = "/daily-rate-input".encodeUrlParameter()
                window.location.href = "/passport-upload?after=$after&autoCreate=1"
                createCarViewModel.reset()
            }
            is CreateCarFromDailyRateState.Success -> {
                createCarViewModel.reset()
                onDailyRateEntered()
            }
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        if (autoCreateProcessed.value) return@LaunchedEffect

        val autoCreate = getUrlParameter("autoCreate")
        if (autoCreate == "1") {
            autoCreateProcessed.value = true
            window.history.replaceState(null, "", "/daily-rate-input")
            createCarViewModel.createFromSavedDailyRate()
        }
    }

    PageWithLogo {
        CenteredFormContainer {
            PageHeader {
                TextSmartHeader("Оплата за день")
            }

            FormSection {
                TextInputField(
                    label = "Оплата за день (₽)",
                    value = dailyRate,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() || it == '.' || it == ',' }) {
                            dailyRate = newValue.replace(',', '.')
                            localError = null
                        }
                    },
                    numeric = true,
                )

                if (error != null) {
                    TextError(error ?: "Произошла ошибка")
                }

                Div({
                    style {
                        display(DisplayStyle.Flex)
                        justifyContent(JustifyContent.Center)
                        marginTop(16.px)
                    }
                }) {
                    Div({
                        style {
                            maxWidth(200.px)
                            width(100.percent)
                        }
                    }) {
                        ActionButton(
                            viewModel = buttonViewModel,
                            enabledColor = CSSColors.Blue,
                            text = if (isLoading) "Создание..." else "Создать",
                            onClick = {
                                val rateValue = dailyRate.toDoubleOrNull()
                                if (rateValue != null && rateValue >= 0) {
                                    createCarViewModel.submitDailyRate(rateValue)
                                } else {
                                    localError = "Введите корректное значение"
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
