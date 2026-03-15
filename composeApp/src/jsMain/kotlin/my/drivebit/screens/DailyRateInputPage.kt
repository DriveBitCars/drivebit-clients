package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import my.drivebit.components.ActionButton
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.FormSection
import my.drivebit.components.PageWithLogo
import my.drivebit.components.Row
import my.drivebit.components.TextError
import my.drivebit.components.TextInputField
import my.drivebit.components.ToolbarBackArrow
import my.drivebit.design.CSSColors
import my.drivebit.repositories.CarDataRepository
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.createButtonViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.koin.compose.koinInject

@Composable
fun DailyRateInputPage(
    onNavigateToLicensePlate: () -> Unit,
    onBack: () -> Unit = {},
) {
    val carDataRepository: CarDataRepository = koinInject()
    val buttonViewModel = createButtonViewModel()

    var dailyRate by remember {
        val savedRate = carDataRepository.getDailyRate()
        mutableStateOf(savedRate?.toString() ?: "")
    }
    var dailyRate4Days by remember {
        val saved = carDataRepository.getDailyRate4Days()
        mutableStateOf(saved?.toString() ?: "")
    }
    var dailyRate7Days by remember {
        val saved = carDataRepository.getDailyRate7Days()
        mutableStateOf(saved?.toString() ?: "")
    }
    var dailyRate14Days by remember {
        val saved = carDataRepository.getDailyRate14Days()
        mutableStateOf(saved?.toString() ?: "")
    }
    var dailyRate21Days by remember {
        val saved = carDataRepository.getDailyRate21Days()
        mutableStateOf(saved?.toString() ?: "")
    }
    var localError by remember { mutableStateOf<String?>(null) }

    val error = localError

    val isValid = dailyRate.toIntOrNull()?.let { it >= 0 } == true

    buttonViewModel.setState(
        if (isValid) ButtonState.Enabled else ButtonState.Disabled,
    )

    fun parseRate(value: String): Int? = value.takeIf { it.isNotBlank() }?.toIntOrNull()?.takeIf { it >= 0 }

    PageWithLogo {
        CenteredFormContainer {
            ToolbarBackArrow(
                title = "Оплата за день",
                onBackClick = onBack,
            )

            FormSection {
                TextInputField(
                    label = "Оплата за день (₽)",
                    value = dailyRate,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            dailyRate = newValue
                            localError = null
                        }
                    },
                    numeric = true,
                )

                TextInputField(
                    label = "Оплата за 4 дня (₽)",
                    value = dailyRate4Days,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            dailyRate4Days = newValue
                            localError = null
                        }
                    },
                    numeric = true,
                )

                TextInputField(
                    label = "Оплата за 7 дней (₽)",
                    value = dailyRate7Days,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            dailyRate7Days = newValue
                            localError = null
                        }
                    },
                    numeric = true,
                )

                TextInputField(
                    label = "Оплата за 14 дней (₽)",
                    value = dailyRate14Days,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            dailyRate14Days = newValue
                            localError = null
                        }
                    },
                    numeric = true,
                )

                TextInputField(
                    label = "Оплата за 21 день (₽)",
                    value = dailyRate21Days,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            dailyRate21Days = newValue
                            localError = null
                        }
                    },
                    numeric = true,
                )

                if (error != null) {
                    TextError(error ?: "Произошла ошибка")
                }

                Row(
                    justifyContent = JustifyContent.Center,
                    modifier = { marginTop(16.px) },
                ) {
                    Div({
                        style {
                            maxWidth(200.px)
                            width(100.percent)
                        }
                    }) {
                        ActionButton(
                            viewModel = buttonViewModel,
                            enabledColor = CSSColors.Blue,
                            text = "Далее",
                            onClick = {
                                val rateValue = dailyRate.toIntOrNull()
                                if (rateValue != null && rateValue >= 0) {
                                    carDataRepository.saveDailyRate(rateValue)
                                    carDataRepository.saveDailyRate4Days(parseRate(dailyRate4Days))
                                    carDataRepository.saveDailyRate7Days(parseRate(dailyRate7Days))
                                    carDataRepository.saveDailyRate14Days(parseRate(dailyRate14Days))
                                    carDataRepository.saveDailyRate21Days(parseRate(dailyRate21Days))
                                    onNavigateToLicensePlate()
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
