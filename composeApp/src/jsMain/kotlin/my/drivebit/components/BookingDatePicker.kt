@file:Suppress("ktlint:standard:no-wildcard-imports")

package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.utils.addMonths
import my.drivebit.utils.lastDayOfMonth
import my.drivebit.viewmodels.DateFieldViewModel
import my.drivebit.viewmodels.DateTimeFieldViewModel
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLInputElement

@Composable
@Suppress("FunctionName")
fun BookingDatePickerDialog(
    label: String,
    viewModel: DateFieldViewModel,
    minDate: String? = null,
    disabledDates: Set<String> = emptySet(),
    onDateChanged: (String?) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    if (!state.isCalendarOpen) return

    var displayMonth by remember { mutableStateOf(initialDisplayMonth(state.date, minDate)) }

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
                Text(label)
            }

            CalendarMonth(
                displayMonth = displayMonth,
                minDate = minDate,
                disabledDates = disabledDates,
                selectedDate = state.date,
                onMonthChange = { displayMonth = it },
                onDateSelected = { dateStr ->
                    viewModel.setDate(dateStr)
                    onDateChanged(dateStr)
                    viewModel.closeCalendar()
                },
            )

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
                        applyTypography(CSSTypography.Styles.body)
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
            }
        }
    }
}

@Composable
@Suppress("FunctionName")
fun BookingDatePickerForDateTime(
    label: String,
    viewModel: DateTimeFieldViewModel,
    minDate: String? = null,
    disabledDates: Set<String> = emptySet(),
    defaultTime: String = "10:00",
    onDateTimeChanged: (date: String?, time: String?) -> Unit = { _, _ -> },
) {
    val state by viewModel.state.collectAsState()

    if (!state.isCalendarOpen) return

    var displayMonth by remember { mutableStateOf(initialDisplayMonth(state.date, minDate)) }

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
                Text(label)
            }

            CalendarMonth(
                displayMonth = displayMonth,
                minDate = minDate,
                disabledDates = disabledDates,
                selectedDate = state.date,
                onMonthChange = { displayMonth = it },
                onDateSelected = { dateStr ->
                    viewModel.setDate(dateStr)
                    if (state.time == null) viewModel.setTime(defaultTime)
                    val time = state.time ?: defaultTime
                    onDateTimeChanged(dateStr, time)
                    viewModel.closeCalendar()
                },
            )

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
                        applyTypography(CSSTypography.Styles.body)
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
            }
        }
    }
}

@Composable
@Suppress("FunctionName")
fun BookingTimePickerDialog(
    label: String,
    viewModel: DateTimeFieldViewModel,
    defaultTime: String = "10:00",
    onDateTimeChanged: (date: String?, time: String?) -> Unit = { _, _ -> },
) {
    val state by viewModel.state.collectAsState()

    if (!state.isTimePickerOpen) return

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
            viewModel.closeTimePicker()
        }
    }) {
        Div({
            style {
                width(100.percent)
                property("max-width", "320px")
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
                Text(label)
            }

            Div({
                style {
                    width(100.percent)
                    padding(10.px, 14.px)
                    borderRadius(8.px)
                    border(1.px, LineStyle.Solid, CSSColors.Gray300)
                    property("box-sizing", "border-box")
                }
            }) {
                Input(
                    type = InputType.Text,
                    attrs = {
                        attr("type", "time")
                        value(state.time ?: defaultTime)
                        onInput { event ->
                            val newValue = (event.target as HTMLInputElement).value
                            viewModel.setTime(newValue.ifEmpty { null })
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
                        backgroundColor(CSSColors.Blue)
                        color(CSSColors.White)
                        cursor("pointer")
                        applyTypography(CSSTypography.Styles.body)
                        fontSize(CSSTypography.FontSize.sm)
                        fontWeight(CSSTypography.FontWeight.medium)
                        property("transition", "background-color 0.2s ease")
                    }
                    onClick {
                        val date = state.date?.take(10)?.takeIf { it.length == 10 }
                        val time = state.time ?: defaultTime
                        if (date != null && time.isNotEmpty()) {
                            onDateTimeChanged(date, time)
                            viewModel.closeTimePicker()
                        }
                    }
                    onMouseEnter {
                        (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                            "opacity",
                            "0.9",
                        )
                    }
                    onMouseLeave {
                        (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                            "opacity",
                            "1",
                        )
                    }
                }) {
                    Text("Готово")
                }
                Div({
                    style {
                        padding(10.px, 18.px)
                        borderRadius(8.px)
                        backgroundColor(CSSColors.Gray300)
                        color(CSSColors.Black)
                        cursor("pointer")
                        applyTypography(CSSTypography.Styles.body)
                        fontSize(CSSTypography.FontSize.sm)
                        fontWeight(CSSTypography.FontWeight.medium)
                        property("transition", "background-color 0.2s ease")
                    }
                    onClick {
                        viewModel.closeTimePicker()
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
            }
        }
    }
}

private fun initialDisplayMonth(
    selectedDate: String?,
    minDate: String?,
): LocalDate {
    selectedDate?.take(10)?.let { str ->
        if (str.length == 10) {
            runCatching { return LocalDate.parse(str) }
        }
    }
    minDate?.take(10)?.let { str ->
        if (str.length == 10) {
            runCatching { return LocalDate.parse(str) }
        }
    }
    return LocalDate.parse(
        Clock.System
            .now()
            .toString()
            .take(10),
    )
}

@Composable
@Suppress("FunctionName")
private fun CalendarMonth(
    displayMonth: LocalDate,
    minDate: String?,
    disabledDates: Set<String>,
    selectedDate: String?,
    onMonthChange: (LocalDate) -> Unit,
    onDateSelected: (String) -> Unit,
) {
    val firstOfMonth = LocalDate(displayMonth.year, displayMonth.month, 1)
    val startOffset = firstOfMonth.dayOfWeek.ordinal
    val daysInMonth = lastDayOfMonth(displayMonth.year, displayMonth.month)
    val minDateParsed = minDate?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    val monthNames =
        listOf(
            "Январь",
            "Февраль",
            "Март",
            "Апрель",
            "Май",
            "Июнь",
            "Июль",
            "Август",
            "Сентябрь",
            "Октябрь",
            "Ноябрь",
            "Декабрь",
        )
    val weekDayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            gap(12.px)
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.SpaceBetween)
            }
        }) {
            Div({
                style {
                    padding(8.px)
                    cursor("pointer")
                    borderRadius(8.px)
                    applyTypography(CSSTypography.Styles.body)
                    fontSize(CSSTypography.FontSize.sm)
                    color(CSSColors.Blue)
                }
                onClick {
                    onMonthChange(addMonths(firstOfMonth, -1))
                }
            }) {
                Text("‹")
            }
            Span({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    fontSize(CSSTypography.FontSize.base)
                    fontWeight(CSSTypography.FontWeight.semibold)
                    color(CSSColors.Black)
                }
            }) {
                Text("${monthNames[displayMonth.month.number - 1]} ${displayMonth.year}")
            }
            Div({
                style {
                    padding(8.px)
                    cursor("pointer")
                    borderRadius(8.px)
                    applyTypography(CSSTypography.Styles.body)
                    fontSize(CSSTypography.FontSize.sm)
                    color(CSSColors.Blue)
                }
                onClick {
                    onMonthChange(addMonths(firstOfMonth, 1))
                }
            }) {
                Text("›")
            }
        }

        Div({
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Row)
                flexWrap(FlexWrap.Wrap)
            }
        }) {
            weekDayNames.forEach { name ->
                Div({
                    style {
                        property("width", "calc(100% / 7)")
                        property("min-width", "36px")
                        textAlign("center")
                        applyTypography(CSSTypography.Styles.body)
                        fontSize(CSSTypography.FontSize.xs)
                        color(CSSColors.Gray600)
                        padding(4.px)
                        property("box-sizing", "border-box")
                    }
                }) {
                    Text(name)
                }
            }
            repeat(startOffset) {
                Div({
                    style {
                        property("width", "calc(100% / 7)")
                        property("min-width", "36px")
                        height(36.px)
                        property("box-sizing", "border-box")
                    }
                }) {}
            }
            for (day in 1..daysInMonth) {
                val dateStr = "${displayMonth.year}-${displayMonth.month.number.toString().padStart(
                    2,
                    '0',
                )}-${day.toString().padStart(2, '0')}"
                val isDisabled =
                    disabledDates.contains(dateStr) ||
                        (minDateParsed != null && LocalDate.parse(dateStr) < minDateParsed)
                val isSelected = selectedDate?.take(10) == dateStr

                Div({
                    style {
                        property("width", "calc(100% / 7)")
                        property("min-width", "36px")
                        height(36.px)
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        justifyContent(JustifyContent.Center)
                        borderRadius(8.px)
                        applyTypography(CSSTypography.Styles.body)
                        fontSize(CSSTypography.FontSize.sm)
                        property("box-sizing", "border-box")
                        when {
                            isDisabled -> {
                                color(CSSColors.Gray300)
                                cursor("not-allowed")
                            }
                            isSelected -> {
                                backgroundColor(CSSColors.Blue)
                                color(CSSColors.White)
                                cursor("pointer")
                            }
                            else -> {
                                color(CSSColors.Black)
                                cursor("pointer")
                            }
                        }
                    }
                    onClick {
                        if (!isDisabled) {
                            onDateSelected(dateStr)
                        }
                    }
                    onMouseEnter {
                        if (!isDisabled && !isSelected) {
                            (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                                "background-color",
                                CSSColors.Gray300String,
                            )
                        }
                    }
                    onMouseLeave {
                        if (!isDisabled && !isSelected) {
                            (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                                "background-color",
                                "transparent",
                            )
                        }
                    }
                }) {
                    Text(day.toString())
                }
            }
        }
    }
}
