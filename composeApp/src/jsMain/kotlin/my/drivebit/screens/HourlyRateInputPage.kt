package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import my.drivebit.components.ActionButton
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.FormSection
import my.drivebit.components.PageHeader
import my.drivebit.shell.PageWithLogo
import my.drivebit.components.TextError
import my.drivebit.components.TextInputField
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.repositories.CarDataRepository
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.createButtonViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.koin.compose.koinInject

@Composable
fun HourlyRateInputPage(onHourlyRateEntered: () -> Unit = {}) {
    val carDataRepository: CarDataRepository = koinInject()
    val buttonViewModel = createButtonViewModel()

    var hourlyRate by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val isValid = hourlyRate.toIntOrNull()?.let { it >= 0 } == true

    buttonViewModel.setState(
        if (isValid) {
            ButtonState.Enabled
        } else {
            ButtonState.Disabled
        },
    )

    PageWithLogo {
        CenteredFormContainer {
            PageHeader {
                TextSmartHeader("Оплата за час")
            }

            FormSection {
                TextInputField(
                    label = "Оплата за час (₽)",
                    value = hourlyRate,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() || it == '.' || it == ',' }) {
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                hourlyRate = newValue
                            }
                            error = null
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
                            text = "Продолжить",
                            onClick = {
                                val rateValue = hourlyRate.toIntOrNull()
                                if (rateValue != null && rateValue >= 0) {
                                    carDataRepository.saveHourlyRate(rateValue)
                                    onHourlyRateEntered()
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
