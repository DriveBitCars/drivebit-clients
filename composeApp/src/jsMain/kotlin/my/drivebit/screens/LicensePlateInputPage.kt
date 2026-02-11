package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import my.drivebit.components.ActionButton
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.FormSection
import my.drivebit.components.PageHeader
import my.drivebit.components.PageWithLogo
import my.drivebit.components.Row
import my.drivebit.components.TextError
import my.drivebit.components.TextInputField
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.repositories.LicensePlateRepository
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.CreateCarFromDailyRateState
import my.drivebit.viewmodels.CreateCarFromDailyRateViewModel
import my.drivebit.viewmodels.LicensePlateValidator
import my.drivebit.viewmodels.createButtonViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.koin.compose.koinInject

@Composable
fun LicensePlateInputPage(
    onLicensePlateEntered: () -> Unit = {},
    onMissingPassport: () -> Unit = {},
) {
    val licensePlateRepository: LicensePlateRepository = koinInject()
    val createCarViewModel: CreateCarFromDailyRateViewModel = koinInject()
    val createCarState by createCarViewModel.state.collectAsState()
    val buttonViewModel = createButtonViewModel()
    var licensePlate by remember { mutableStateOf(licensePlateRepository.getLicensePlate().orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }

    val isLoading = createCarState is CreateCarFromDailyRateState.Loading
    val errorFromState = (createCarState as? CreateCarFromDailyRateState.Error)?.message
    val isValid = LicensePlateValidator.isValid(licensePlate) && !isLoading

    buttonViewModel.setState(
        when {
            isLoading -> ButtonState.Loading
            isValid -> ButtonState.Enabled
            else -> ButtonState.Disabled
        },
    )

    LaunchedEffect(createCarState) {
        when (createCarState) {
            CreateCarFromDailyRateState.MissingPassport -> {
                createCarViewModel.reset()
                onMissingPassport()
            }
            is CreateCarFromDailyRateState.Success -> {
                createCarViewModel.reset()
                onLicensePlateEntered()
            }
            else -> Unit
        }
    }

    PageWithLogo {
        CenteredFormContainer {
            PageHeader {
                TextSmartHeader("Введите номер")
            }

            FormSection {
                TextInputField(
                    label = "Государственный номер",
                    value = licensePlate,
                    onValueChange = { newValue ->
                        val filtered = LicensePlateValidator.filterInput(newValue)
                        licensePlate = filtered
                        error = if (filtered.isBlank() || LicensePlateValidator.isValid(filtered)) {
                            null
                        } else {
                            LicensePlateValidator.ERROR_MESSAGE
                        }
                    },
                    maxLength = LicensePlateValidator.MAX_LENGTH,
                    errorMessage = error,
                )

                if (errorFromState != null) {
                    TextError(errorFromState)
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
                            text = if (isLoading) "Создание..." else "Создать",
                            onClick = {
                                val plate = licensePlate.trim()
                                if (LicensePlateValidator.isValid(plate)) {
                                    licensePlateRepository.saveLicensePlate(plate)
                                    createCarViewModel.createFromSavedDailyRate()
                                } else {
                                    error = if (plate.isEmpty()) {
                                        "Введите номер"
                                    } else {
                                        LicensePlateValidator.ERROR_MESSAGE
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
