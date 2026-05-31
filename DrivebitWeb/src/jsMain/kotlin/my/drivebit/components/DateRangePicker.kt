package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.viewmodels.DateRangePickerViewModel
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLInputElement

@Composable
fun DateRangePicker(
    viewModel: DateRangePickerViewModel,
    onDateRangeSelected: (startDate: String?, endDate: String?) -> Unit,
    onSearchClick: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val localStartDate = state.startDate ?: ""
    val localEndDate = state.endDate ?: ""

    Div({
        style {
            width(100.percent)
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            gap(12.px)
            backgroundColor(CSSColors.White)
            borderRadius(8.px)
            padding(12.px, 16.px)
            border(1.px, LineStyle.Solid, CSSColors.Gray300)
            cursor("pointer")
            property("transition", "border-color 0.2s ease")
        }
        onClick {
            viewModel.openCalendar()
        }
        onMouseEnter {
            (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                "border-color",
                CSSColors.BlueString,
            )
        }
        onMouseLeave {
            (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                "border-color",
                CSSColors.Gray300String,
            )
        }
    }) {
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
                    fontSize(CSSTypography.FontSize.sm)
                    color(CSSColors.Gray600)
                }
            }) {
                Text("c")
            }

            if (localStartDate.isNotEmpty() || localEndDate.isNotEmpty()) {
                Span({
                    style {
                        applyTypography(CSSTypography.Styles.body)
                        fontSize(CSSTypography.FontSize.base)
                        color(CSSColors.Black)
                        fontWeight(CSSTypography.FontWeight.medium)
                    }
                }) {
                    val dateText =
                        buildString {
                            if (localStartDate.isNotEmpty()) {
                                append(formatDateForDisplay(localStartDate))
                            }
                            if (localStartDate.isNotEmpty() && localEndDate.isNotEmpty()) {
                                append(" - ")
                            }
                            if (localEndDate.isNotEmpty()) {
                                append(formatDateForDisplay(localEndDate))
                            }
                        }
                    Text(dateText)
                }
            }
        }
    }
}

@Composable
fun DateRangePickerDialog(
    viewModel: DateRangePickerViewModel,
    onDateRangeSelected: (startDate: String?, endDate: String?) -> Unit,
    onSearchClick: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val startDate = state.startDate ?: ""
    val endDate = state.endDate ?: ""
    Div({
        style {
            position(Position.Fixed)
            top(0.px)
            left(0.px)
            right(0.px)
            bottom(0.px)
            property("background-color", "rgba(0, 0, 0, 0.5)")
            property("z-index", "1000")
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.Center)
            padding(16.px)
        }
        onClick {
            viewModel.closeCalendar()
        }
    }) {
        Div({
            style {
                width(100.percent)
                property("max-width", "360px")
                backgroundColor(CSSColors.White)
                borderRadius(12.px)
                padding(18.px)
                border(1.px, LineStyle.Solid, CSSColors.Gray300)
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                gap(16.px)
                property("box-shadow", "0 8px 24px rgba(0, 0, 0, 0.2)")
                property("margin", "0 12px")
            }
            onClick { event ->
                event.stopPropagation()
            }
        }) {
            Span({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    fontSize(CSSTypography.FontSize.base)
                    fontWeight(CSSTypography.FontWeight.semibold)
                    color(CSSColors.Black)
                    marginBottom(4.px)
                }
            }) {
                Text("Выберите период аренды")
            }

            Div({
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    gap(10.px)
                }
            }) {
                Span({
                    style {
                        applyTypography(CSSTypography.Styles.body)
                        fontSize(CSSTypography.FontSize.sm)
                        fontWeight(CSSTypography.FontWeight.medium)
                        color(CSSColors.Black)
                    }
                }) {
                    Text("Дата начала")
                }

                Input(
                    type = InputType.Date,
                    attrs = {
                        value(startDate)
                        onInput { event ->
                            val newValue = (event.target as HTMLInputElement).value
                            val date = if (newValue.isEmpty()) null else newValue
                            viewModel.setStartDate(date)
                            onDateRangeSelected(viewModel.state.value.startDate, viewModel.state.value.endDate)
                        }
                        style {
                            width(100.percent)
                            padding(10.px, 14.px)
                            borderRadius(8.px)
                            border(1.px, LineStyle.Solid, CSSColors.Gray300)
                            applyTypography(CSSTypography.Styles.body)
                            fontSize(CSSTypography.FontSize.sm)
                            color(CSSColors.Black)
                            property("box-sizing", "border-box")
                            property("outline", "none")
                            property("transition", "border-color 0.2s ease")
                        }
                        onFocus {
                            (it.target as HTMLInputElement).style.setProperty(
                                "border-color",
                                CSSColors.BlueString,
                            )
                        }
                        onBlur {
                            (it.target as HTMLInputElement).style.setProperty(
                                "border-color",
                                CSSColors.Gray300String,
                            )
                        }
                    },
                )
            }

            Div({
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    gap(10.px)
                }
            }) {
                Span({
                    style {
                        applyTypography(CSSTypography.Styles.body)
                        fontSize(CSSTypography.FontSize.sm)
                        fontWeight(CSSTypography.FontWeight.medium)
                        color(CSSColors.Black)
                    }
                }) {
                    Text("Дата окончания")
                }

                Input(
                    type = InputType.Date,
                    attrs = {
                        value(endDate)
                        startDate.takeIf { it.isNotEmpty() }?.let { minDate ->
                            attr("min", minDate)
                        }
                        onInput { event ->
                            val newValue = (event.target as HTMLInputElement).value
                            val date = if (newValue.isEmpty()) null else newValue
                            viewModel.setEndDate(date)
                            onDateRangeSelected(viewModel.state.value.startDate, viewModel.state.value.endDate)
                        }
                        style {
                            width(100.percent)
                            padding(10.px, 14.px)
                            borderRadius(8.px)
                            border(1.px, LineStyle.Solid, CSSColors.Gray300)
                            applyTypography(CSSTypography.Styles.body)
                            fontSize(CSSTypography.FontSize.sm)
                            color(CSSColors.Black)
                            property("box-sizing", "border-box")
                            property("outline", "none")
                            property("transition", "border-color 0.2s ease")
                        }
                        onFocus {
                            (it.target as HTMLInputElement).style.setProperty(
                                "border-color",
                                CSSColors.BlueString,
                            )
                        }
                        onBlur {
                            (it.target as HTMLInputElement).style.setProperty(
                                "border-color",
                                CSSColors.Gray300String,
                            )
                        }
                    },
                )
            }

            Div({
                style {
                    display(DisplayStyle.Flex)
                    justifyContent(JustifyContent.FlexEnd)
                    gap(8.px)
                }
            }) {
                Div({
                    style {
                        padding(10.px, 18.px)
                        borderRadius(8.px)
                        backgroundColor(CSSColors.Gray300)
                        color(CSSColors.Black)
                        cursor("pointer")
                        applyTypography(CSSTypography.Styles.button)
                        fontSize(CSSTypography.FontSize.sm)
                        fontWeight(CSSTypography.FontWeight.medium)
                        property("transition", "background-color 0.2s ease")
                    }
                    onClick {
                        viewModel.closeCalendar()
                    }
                    onMouseEnter {
                        (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                            "background-color",
                            CSSColors.Gray600String,
                        )
                    }
                    onMouseLeave {
                        (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                            "background-color",
                            CSSColors.Gray300String,
                        )
                    }
                }) {
                    Text("Отмена")
                }

                Div({
                    style {
                        padding(10.px, 18.px)
                        borderRadius(8.px)
                        backgroundColor(CSSColors.Black)
                        color(CSSColors.White)
                        cursor("pointer")
                        applyTypography(CSSTypography.Styles.button)
                        fontSize(CSSTypography.FontSize.sm)
                        fontWeight(CSSTypography.FontWeight.semibold)
                        property("transition", "background-color 0.2s ease")
                    }
                    onClick {
                        viewModel.closeCalendar()
                        onSearchClick()
                    }
                    onMouseEnter {
                        (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                            "background-color",
                            CSSColors.BlueRedString,
                        )
                    }
                    onMouseLeave {
                        (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                            "background-color",
                            CSSColors.BlackString,
                        )
                    }
                }) {
                    Text("Поиск")
                }
            }
        }
    }
}

private fun formatDateForDisplay(dateString: String): String {
    if (dateString.isEmpty()) return ""
    return try {
        val date = LocalDate.parse(dateString)
        val day = date.day.toString().padStart(2, '0')
        val month =
            date.month.number
                .toString()
                .padStart(2, '0')
        val year = date.year
        "$day.$month.$year"
    } catch (e: Exception) {
        dateString
    }
}
