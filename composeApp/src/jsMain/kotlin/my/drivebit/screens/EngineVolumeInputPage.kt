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
import my.drivebit.components.PageWithLogo
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
fun EngineVolumeInputPage(onVolumeEntered: () -> Unit = {}) {
    val carDataRepository: CarDataRepository = koinInject()
    val buttonViewModel = createButtonViewModel()
    var volume by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val isValid = volume.toDoubleOrNull()?.let { it > 0 && it <= 20 } == true

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
                TextSmartHeader("Объем двигателя")
            }

            FormSection {
                TextInputField(
                    label = "Объем двигателя (л)",
                    value = volume,
                    onValueChange = { newValue ->
                        val cleanValue = newValue.replace(",", ".")
                        if (cleanValue.isEmpty() || cleanValue.toDoubleOrNull() != null) {
                            volume = cleanValue
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
                                val volumeValue = volume.toDoubleOrNull()
                                if (volumeValue != null && volumeValue > 0 && volumeValue <= 20) {
                                    carDataRepository.saveEngineVolume(volumeValue)
                                    onVolumeEntered()
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
