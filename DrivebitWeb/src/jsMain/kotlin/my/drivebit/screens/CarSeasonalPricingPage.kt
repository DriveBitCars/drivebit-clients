package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.browser.window
import my.drivebit.components.ActionButton
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.Column
import my.drivebit.components.FormSection
import my.drivebit.components.Loader
import my.drivebit.shell.PageWithLogo
import my.drivebit.components.Row
import my.drivebit.components.TextError
import my.drivebit.components.TextInputField
import my.drivebit.components.ToolbarBackArrow
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.utils.getUrlParameter
import my.drivebit.viewmodels.CarSeasonalPricingState
import my.drivebit.viewmodels.CarSeasonalPricingViewModel
import my.drivebit.viewmodels.SeasonalPriceAdjustmentFormPeriod
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.currentKoinScope
import org.koin.core.parameter.parametersOf
import org.w3c.dom.HTMLInputElement

@Composable
fun CarSeasonalPricingPage() {
    val carIdParam = getUrlParameter("carId")

    if (carIdParam.isBlank()) {
        PageWithLogo {
            CenteredFormContainer {
                ToolbarBackArrow(
                    title = "Сезонные наценки",
                    onBackClick = {},
                )
                FormSection {
                    TextError("Не указан ID автомобиля")
                }
            }
        }
        return
    }

    val koinScope = currentKoinScope()
    val viewModel: CarSeasonalPricingViewModel =
        remember(carIdParam) {
            koinScope.get<CarSeasonalPricingViewModel> { parametersOf(carIdParam) }
        }
    val state by viewModel.state.collectAsState()

    PageWithLogo {
        CenteredFormContainer {
            ToolbarBackArrow(
                title = "Сезонные наценки",
                onBackClick = {
                    window.location.href = "/car-edit?id=$carIdParam"
                },
            )

            FormSection {
                when (val currentState = state) {
                    is CarSeasonalPricingState.Loading -> {
                        Loader()
                    }

                    is CarSeasonalPricingState.Error -> {
                        TextError(currentState.message)
                    }

                    is CarSeasonalPricingState.Ready -> {
                        Column(gap = 16.px) {
                            Div({
                                style {
                                    applyTypography(CSSTypography.Styles.body)
                                    color(CSSColors.Gray600)
                                }
                            }) {
                                Text("Периоды с процентом наценки или скидки. Пустой список снимает все наценки.")
                            }

                            if (currentState.periods.isEmpty()) {
                                Div({
                                    style {
                                        applyTypography(CSSTypography.Styles.body)
                                        color(CSSColors.Gray600)
                                    }
                                }) {
                                    Text("Периодов нет")
                                }
                            } else {
                                Column(gap = 12.px) {
                                    currentState.periods.forEach { period ->
                                        SeasonalPeriodRow(
                                            period = period,
                                            onStartChange = { viewModel.updatePeriod(key = period.key, startsAt = it) },
                                            onEndChange = { viewModel.updatePeriod(key = period.key, endsAt = it) },
                                            onPercentChange = { viewModel.updatePeriod(key = period.key, percent = it) },
                                            onDelete = { viewModel.removePeriod(period.key) },
                                        )
                                    }
                                }
                            }

                            if (currentState.formError != null) {
                                TextError(currentState.formError ?: "")
                            }

                            if (currentState.saved) {
                                Div({
                                    style {
                                        applyTypography(CSSTypography.Styles.body)
                                        color(CSSColors.Green)
                                    }
                                }) {
                                    Text("Сохранено")
                                }
                            }

                            ActionButton(
                                enabledColor = CSSColors.Blue,
                                text = "Добавить период",
                                onClick = { viewModel.addPeriod() },
                            )
                            ActionButton(
                                enabledColor = CSSColors.Blue,
                                text = if (currentState.isSaving) "Сохранение..." else "Сохранить",
                                onClick = { viewModel.save() },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeasonalPeriodRow(
    period: SeasonalPriceAdjustmentFormPeriod,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    onPercentChange: (String) -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        gap = 8.px,
        modifier = {
            padding(12.px)
            borderRadius(8.px)
            property("border", "1px solid ${CSSColors.Gray300String}")
        },
    ) {
        Row(gap = 12.px) {
            PeriodDateField(label = "С", value = period.startsAt, onChange = onStartChange)
            PeriodDateField(label = "По", value = period.endsAt, onChange = onEndChange)
        }
        TextInputField(
            label = "Наценка, %",
            value = period.percent,
            onValueChange = { newValue ->
                val cleaned =
                    newValue.filterIndexed { index, c ->
                        c.isDigit() || (index == 0 && c == '-')
                    }
                if (cleaned.isEmpty() || cleaned == "-" || cleaned.toIntOrNull() != null) {
                    onPercentChange(cleaned)
                }
            },
            numeric = true,
        )
        ActionButton(
            enabledColor = CSSColors.Red,
            text = "Удалить период",
            onClick = onDelete,
        )
    }
}

@Composable
private fun PeriodDateField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
) {
    Div({
        style {
            flex(1)
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            gap(4.px)
        }
    }) {
        Span({
            style {
                applyTypography(CSSTypography.Styles.body)
                fontSize(CSSTypography.FontSize.xs)
                color(CSSColors.Black)
            }
        }) {
            Text(label)
        }
        Input(type = InputType.Date) {
            value(value)
            onInput { event ->
                onChange((event.target as HTMLInputElement).value)
            }
            style {
                width(100.percent)
                padding(10.px, 14.px)
                borderRadius(8.px)
                border(1.px, LineStyle.Solid, CSSColors.Gray300)
                applyTypography(CSSTypography.Styles.body)
                property("box-sizing", "border-box")
            }
        }
    }
}
