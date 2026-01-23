package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.browser.window
import my.drivebit.components.ActionButton
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.FormSection
import my.drivebit.components.Loader
import my.drivebit.components.PageHeader
import my.drivebit.components.PageWithLogo
import my.drivebit.components.StringList
import my.drivebit.components.TextError
import my.drivebit.components.TextInputField
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.utils.getUrlParameter
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.CarEditIntent
import my.drivebit.viewmodels.CarEditMviState
import my.drivebit.viewmodels.CarEditMviViewModel
import my.drivebit.viewmodels.createButtonViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.koin.compose.currentKoinScope
import org.koin.core.parameter.parametersOf

@Composable
fun CarEditPage() {
    val carIdParam = getUrlParameter("id")

    if (carIdParam.isBlank()) {
        PageWithLogo {
            CenteredFormContainer {
                PageHeader {
                    TextSmartHeader("Управление автомобилем")
                }
                FormSection {
                    TextError("Не указан ID автомобиля")
                }
            }
        }
        return
    }

    val koinScope = currentKoinScope()

    val viewModel: CarEditMviViewModel =
        remember(carIdParam) {
            koinScope.get<CarEditMviViewModel> { parametersOf(carIdParam) }
        }
    val buttonViewModel = createButtonViewModel()
    val navigationController = LocalNavigationController.current

    val state by viewModel.state.collectAsState()

    PageWithLogo {
        CenteredFormContainer {
            PageHeader {
                TextSmartHeader("Управление автомобилем")
            }

            FormSection {
                when (val currentState = state) {
                    is CarEditMviState.Loading -> {
                        Loader()
                    }

                    is CarEditMviState.Error -> {
                        TextError(currentState.message)
                    }

                    is CarEditMviState.NavigateToMyCars -> {
                        navigationController?.navigateTo("/my-cars")
                    }

                    is CarEditMviState.Success -> {
                        buttonViewModel.setState(
                            if (currentState.isSaveButtonEnabled) {
                                ButtonState.Enabled
                            } else {
                                ButtonState.Disabled
                            },
                        )
                        val formData = currentState.formData

                        Div({
                            style {
                                display(DisplayStyle.Flex)
                                flexDirection(FlexDirection.Column)
                                gap(16.px)
                            }
                        }) {
                            TextInputField(
                                label = "Госномер",
                                value = formData.licensePlate,
                                onValueChange = { viewModel.handleIntent(CarEditIntent.UpdateLicensePlate(it)) },
                            )

                            TextInputField(
                                label = "Марка",
                                value = formData.brandSearch,
                                onValueChange = { newValue ->
                                    viewModel.handleIntent(CarEditIntent.UpdateBrandSearch(newValue))
                                },
                                onFocus = {
                                    currentState.brandBlurTimeout?.let { window.clearTimeout(it) }
                                    viewModel.handleIntent(CarEditIntent.SetBrandBlurTimeout(null))
                                    viewModel.handleIntent(CarEditIntent.SetBrandFocus(true))
                                },
                                onBlur = {
                                    val timeout =
                                        window.setTimeout({
                                            viewModel.handleIntent(CarEditIntent.SetBrandFocus(false))
                                        }, 200)
                                    viewModel.handleIntent(CarEditIntent.SetBrandBlurTimeout(timeout))
                                },
                            )
                            if (currentState.brands.isNotEmpty() && currentState.isBrandFocused) {
                                StringList(
                                    strings = currentState.brands.map { it.name },
                                    onSelected = { brandName ->
                                        val brand = currentState.brands.find { it.name == brandName }
                                        brand?.let {
                                            viewModel.handleIntent(CarEditIntent.SelectBrand(brand.id, brand.name))
                                        }
                                    },
                                )
                            }

                            if (formData.brandId != null) {
                                TextInputField(
                                    label = "Модель",
                                    value = formData.modelSearch,
                                    onValueChange = { newValue ->
                                        viewModel.handleIntent(CarEditIntent.UpdateModelSearch(newValue))
                                    },
                                    onFocus = {
                                        currentState.modelBlurTimeout?.let { window.clearTimeout(it) }
                                        viewModel.handleIntent(CarEditIntent.SetModelBlurTimeout(null))
                                        viewModel.handleIntent(CarEditIntent.SetModelFocus(true))
                                    },
                                    onBlur = {
                                        val timeout =
                                            window.setTimeout({
                                                viewModel.handleIntent(CarEditIntent.SetModelFocus(false))
                                            }, 200)
                                        viewModel.handleIntent(CarEditIntent.SetModelBlurTimeout(timeout))
                                    },
                                )
                                if (currentState.models.isNotEmpty() && currentState.isModelFocused) {
                                    StringList(
                                        strings = currentState.models.map { it.name },
                                        onSelected = { modelName ->
                                            val model = currentState.models.find { it.name == modelName }
                                            model?.let {
                                                viewModel.handleIntent(CarEditIntent.SelectModel(model.id, model.name))
                                            }
                                        },
                                    )
                                }
                            }

                            TextInputField(
                                label = "Тип кузова",
                                value = formData.bodyTypeSearch,
                                onValueChange = { newValue ->
                                    viewModel.handleIntent(CarEditIntent.UpdateBodyTypeSearch(newValue))
                                },
                                onFocus = {
                                    currentState.bodyTypeBlurTimeout?.let { window.clearTimeout(it) }
                                    viewModel.handleIntent(CarEditIntent.SetBodyTypeBlurTimeout(null))
                                    viewModel.handleIntent(CarEditIntent.SetBodyTypeFocus(true))
                                    if (formData.bodyTypeTranslate.isNotBlank()) {
                                        viewModel.handleIntent(CarEditIntent.UpdateBodyTypeSearch(""))
                                    }
                                },
                                onBlur = {
                                    val timeout =
                                        window.setTimeout({
                                            viewModel.handleIntent(CarEditIntent.SetBodyTypeFocus(false))
                                        }, 200)
                                    viewModel.handleIntent(CarEditIntent.SetBodyTypeBlurTimeout(timeout))
                                },
                            )
                            if (currentState.bodyTypes.isNotEmpty() && currentState.isBodyTypeFocused) {
                                currentState.bodyTypeBlurTimeout?.let { window.clearTimeout(it) }
                                StringList(
                                    strings = currentState.bodyTypes.map { it.translate },
                                    onSelected = { translate ->
                                        currentState.bodyTypeBlurTimeout?.let { window.clearTimeout(it) }
                                        val bodyType = currentState.bodyTypes.find { it.translate == translate }
                                        bodyType?.let {
                                            viewModel.handleIntent(CarEditIntent.SetBodyTypeBlurTimeout(null))
                                            viewModel.handleIntent(
                                                CarEditIntent.SelectBodyType(bodyType.name, bodyType.translate),
                                            )
                                        }
                                    },
                                )
                            }

                            TextInputField(
                                label = "Привод",
                                value = formData.driveTypeSearch,
                                onValueChange = { newValue ->
                                    viewModel.handleIntent(CarEditIntent.UpdateDriveTypeSearch(newValue))
                                },
                                onFocus = {
                                    currentState.driveTypeBlurTimeout?.let { window.clearTimeout(it) }
                                    viewModel.handleIntent(CarEditIntent.SetDriveTypeBlurTimeout(null))
                                    viewModel.handleIntent(CarEditIntent.SetDriveTypeFocus(true))
                                    if (formData.driveTypeTranslate.isNotBlank()) {
                                        viewModel.handleIntent(CarEditIntent.UpdateDriveTypeSearch(""))
                                    }
                                },
                                onBlur = {
                                    val timeout =
                                        window.setTimeout({
                                            viewModel.handleIntent(CarEditIntent.SetDriveTypeFocus(false))
                                        }, 200)
                                    viewModel.handleIntent(CarEditIntent.SetDriveTypeBlurTimeout(timeout))
                                },
                            )
                            if (currentState.driveTypes.isNotEmpty() && currentState.isDriveTypeFocused) {
                                currentState.driveTypeBlurTimeout?.let { window.clearTimeout(it) }
                                StringList(
                                    strings = currentState.driveTypes.map { it.translate },
                                    onSelected = { translate ->
                                        currentState.driveTypeBlurTimeout?.let { window.clearTimeout(it) }
                                        val driveType = currentState.driveTypes.find { it.translate == translate }
                                        driveType?.let {
                                            viewModel.handleIntent(CarEditIntent.SetDriveTypeBlurTimeout(null))
                                            viewModel.handleIntent(
                                                CarEditIntent.SelectDriveType(driveType.name, driveType.translate),
                                            )
                                        }
                                    },
                                )
                            }

                            TextInputField(
                                label = "Тип двигателя",
                                value = formData.engineTypeSearch,
                                onValueChange = { newValue ->
                                    viewModel.handleIntent(CarEditIntent.UpdateEngineTypeSearch(newValue))
                                },
                                onFocus = {
                                    currentState.engineTypeBlurTimeout?.let { window.clearTimeout(it) }
                                    viewModel.handleIntent(CarEditIntent.SetEngineTypeBlurTimeout(null))
                                    viewModel.handleIntent(CarEditIntent.SetEngineTypeFocus(true))
                                    if (formData.engineTypeTranslate.isNotBlank()) {
                                        viewModel.handleIntent(CarEditIntent.UpdateEngineTypeSearch(""))
                                    }
                                },
                                onBlur = {
                                    val timeout =
                                        window.setTimeout({
                                            viewModel.handleIntent(CarEditIntent.SetEngineTypeFocus(false))
                                        }, 200)
                                    viewModel.handleIntent(CarEditIntent.SetEngineTypeBlurTimeout(timeout))
                                },
                            )
                            if (currentState.engineTypes.isNotEmpty() && currentState.isEngineTypeFocused) {
                                currentState.engineTypeBlurTimeout?.let { window.clearTimeout(it) }
                                StringList(
                                    strings = currentState.engineTypes.map { it.translate },
                                    onSelected = { translate ->
                                        currentState.engineTypeBlurTimeout?.let { window.clearTimeout(it) }
                                        val engineType = currentState.engineTypes.find { it.translate == translate }
                                        engineType?.let {
                                            viewModel.handleIntent(CarEditIntent.SetEngineTypeBlurTimeout(null))
                                            viewModel.handleIntent(
                                                CarEditIntent.SelectEngineType(engineType.name, engineType.translate),
                                            )
                                        }
                                    },
                                )
                            }

                            TextInputField(
                                label = "Объем двигателя (л)",
                                value = formData.engineVolume,
                                onValueChange = { newValue ->
                                    val cleanValue = newValue.replace(",", ".")
                                    if (cleanValue.isEmpty() || cleanValue.toDoubleOrNull() != null) {
                                        viewModel.handleIntent(CarEditIntent.UpdateEngineVolume(cleanValue))
                                    }
                                },
                            )

                            // TextInputField(
                            //     label = "Оплата за час (₽)",
                            //     value = formData.hourlyRate,
                            //     onValueChange = { newValue ->
                            //         val cleanValue = newValue.replace(",", ".")
                            //         if (cleanValue.isEmpty() || cleanValue.toDoubleOrNull() != null) {
                            //             viewModel.handleIntent(CarEditIntent.UpdateHourlyRate(cleanValue))
                            //         }
                            //     },
                            //     numeric = true,
                            // )

                            TextInputField(
                                label = "Оплата за день (₽)",
                                value = formData.dailyRate,
                                onValueChange = { newValue ->
                                    val cleanValue = newValue.replace(",", ".")
                                    if (cleanValue.isEmpty() || cleanValue.toDoubleOrNull() != null) {
                                        viewModel.handleIntent(CarEditIntent.UpdateDailyRate(cleanValue))
                                    }
                                },
                                numeric = true,
                            )

                            // TextInputField(
                            //    label = "Год выпуска",
                            //    value = formData.productionYear,
                            //    onValueChange = { newValue ->
                            //        viewModel.handleIntent(CarEditIntent.UpdateProductionYear(newValue))
                            //    },
                            //    maxLength = 4,
                            //    numeric = true,
                            // )

                            // TextInputField(
                            //    label = "Количество мест",
                            //    value = formData.seatsCount,
                            //    onValueChange = { newValue ->
                            //        viewModel.handleIntent(CarEditIntent.UpdateSeatsCount(newValue))
                            //    },
                            //    numeric = true,
                            // )

                            Div({
                                style {
                                    display(DisplayStyle.Flex)
                                    flexDirection(FlexDirection.Row)
                                    gap(12.px)
                                }
                            }) {
                                ActionButton(
                                    enabledColor = CSSColors.Blue,
                                    text = "Управление фотографиями",
                                    onClick = {
                                        window.location.href = "/car-photos?carId=${currentState.carId}"
                                    },
                                )
                                ActionButton(
                                    enabledColor = CSSColors.Red,
                                    text = "Удалить",
                                    onClick = {
                                        viewModel.handleIntent(CarEditIntent.DeleteCar)
                                    },
                                )
                            }

                            if (currentState.saveError != null) {
                                TextError(currentState.saveError ?: "Ошибка сохранения")
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
                                        text = "Сохранить",
                                        onClick = {
                                            viewModel.handleIntent(CarEditIntent.SaveCar)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
