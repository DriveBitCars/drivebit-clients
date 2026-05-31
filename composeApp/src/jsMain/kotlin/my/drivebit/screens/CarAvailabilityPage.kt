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
import my.drivebit.components.SearchDateRangeSelector
import my.drivebit.components.TextError
import my.drivebit.components.ToolbarBackArrow
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.utils.formatAvailabilityBlockPeriod
import my.drivebit.utils.getUrlParameter
import my.drivebit.viewmodels.CarAvailabilityState
import my.drivebit.viewmodels.CarAvailabilityViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.currentKoinScope
import org.koin.core.parameter.parametersOf

@Composable
fun CarAvailabilityPage() {
    val carIdParam = getUrlParameter("carId")

    if (carIdParam.isBlank()) {
        PageWithLogo {
            CenteredFormContainer {
                ToolbarBackArrow(
                    title = "Календарь доступности",
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
    val viewModel: CarAvailabilityViewModel =
        remember(carIdParam) {
            koinScope.get<CarAvailabilityViewModel> { parametersOf(carIdParam) }
        }
    val state by viewModel.state.collectAsState()

    PageWithLogo {
        CenteredFormContainer {
            ToolbarBackArrow(
                title = "Календарь доступности",
                onBackClick = {
                    window.location.href = "/car-edit?id=$carIdParam"
                },
            )

            FormSection {
                when (val currentState = state) {
                    is CarAvailabilityState.Loading -> {
                        Loader()
                    }

                    is CarAvailabilityState.Error -> {
                        TextError(currentState.message)
                    }

                    is CarAvailabilityState.Success -> {
                        Column(gap = 16.px) {
                            SearchDateRangeSelector(
                                startDate = currentState.selectedStartDate,
                                endDate = currentState.selectedEndDate,
                                onStartDateChanged = viewModel::updateSelectedStartDate,
                                onEndDateChanged = viewModel::updateSelectedEndDate,
                                disabledDates = currentState.disabledDates,
                                endMinOffsetDaysFromStart = 0,
                                clearRangeOnCancel = false,
                            )

                            ActionButton(
                                enabledColor = CSSColors.Blue,
                                text = if (currentState.isSaving) "Сохранение..." else "Заблокировать даты",
                                onClick = { viewModel.createBlock() },
                            )

                            if (currentState.formError != null) {
                                TextError(currentState.formError ?: "")
                            }

                            if (currentState.blocks.isEmpty()) {
                                Div({
                                    style {
                                        applyTypography(CSSTypography.Styles.body)
                                        color(CSSColors.Gray600)
                                    }
                                }) {
                                    Text("Нет заблокированных дат")
                                }
                            } else {
                                Column(gap = 12.px) {
                                    currentState.blocks.forEach { block ->
                                        AvailabilityBlockRow(
                                            period =
                                                formatAvailabilityBlockPeriod(
                                                    startAt = block.startAt,
                                                    endAt = block.endAt,
                                                ),
                                            typeLabel = block.blockTypeTranslate ?: block.blockType ?: "Блокировка",
                                            onDelete = { viewModel.deleteBlock(block.id) },
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
}

@Composable
private fun AvailabilityBlockRow(
    period: String,
    typeLabel: String,
    onDelete: () -> Unit,
) {
    Row(
        gap = 12.px,
        alignItems = AlignItems.Center,
        modifier = {
            padding(12.px)
            borderRadius(8.px)
            property("border", "1px solid ${CSSColors.Gray300String}")
        },
    ) {
        Column(gap = 4.px) {
            Div({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    fontWeight("600")
                }
            }) {
                Text(period)
            }
            Div({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    color(CSSColors.Gray600)
                }
            }) {
                Text(typeLabel)
            }
        }
        ActionButton(
            enabledColor = CSSColors.Red,
            text = "Удалить",
            onClick = onDelete,
        )
    }
}
