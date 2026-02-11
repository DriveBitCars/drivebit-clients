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
fun SeatsCountInputPage(
    onSeatsCountEntered: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val carDataRepository: CarDataRepository = koinInject()
    val buttonViewModel = createButtonViewModel()

    var seatsCount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val isValid = seatsCount.toIntOrNull()?.let { it >= 1 && it <= 50 } == true

    buttonViewModel.setState(
        if (isValid) {
            ButtonState.Enabled
        } else {
            ButtonState.Disabled
        },
    )

    PageWithLogo {
        CenteredFormContainer {
            ToolbarBackArrow(
                title = "Количество мест",
                onBackClick = onBack,
            )

            FormSection {
                TextInputField(
                    label = "Количество мест",
                    value = seatsCount,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            seatsCount = newValue
                            error = null
                        }
                    },
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
                                val seatsValue = seatsCount.toIntOrNull()
                                if (seatsValue != null && seatsValue >= 1 && seatsValue <= 50) {
                                    carDataRepository.saveSeatsCount(seatsValue)
                                    onSeatsCountEntered()
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
