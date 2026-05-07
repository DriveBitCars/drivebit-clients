package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.utils.addDays
import my.drivebit.utils.addMonths
import my.drivebit.utils.lastDayOfMonth
import my.drivebit.viewmodels.DateFieldViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun DateRangeCalendarDialog(
    startDateViewModel: DateFieldViewModel,
    endDateViewModel: DateFieldViewModel,
    minDate: String? = null,
    disabledDates: Set<String> = emptySet(),
    /** Минимум дней после начала для даты окончания (1 = конец не раньше чем следующий день после начала). */
    endMinOffsetDaysFromStart: Int = 0,
    /** Если false, «Отмена» только закрывает диалог, не обнуляет выбранный диапазон (как в бронировании). */
    clearRangeOnCancel: Boolean = true,
    onConfirm: (() -> Unit)? = null,
) {
    val startState by startDateViewModel.state.collectAsState()
    val endState by endDateViewModel.state.collectAsState()
    val open = startState.isCalendarOpen || endState.isCalendarOpen
    if (!open) return

    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val minParsed =
        minDate?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: today

    var displayMonth by remember { mutableStateOf(today) }

    LaunchedEffect(open) {
        if (open) {
            displayMonth =
                parseIsoLocalDate(endState.date)
                    ?: parseIsoLocalDate(startState.date)
                    ?: minParsed
        }
    }

    val closeAll = {
        startDateViewModel.closeCalendar()
        endDateViewModel.closeCalendar()
    }

    val onDayClick: (LocalDate) -> Unit = { clicked ->
        val dateStr = clicked.toIsoString()
        if (clicked >= minParsed && dateStr !in disabledDates) {
            val start = parseIsoLocalDate(startState.date)
            val end = parseIsoLocalDate(endState.date)
            when {
                start == null -> {
                    startDateViewModel.setDate(dateStr)
                    endDateViewModel.setDate(null)
                }
                end == null -> {
                    when {
                        clicked < start -> {
                            startDateViewModel.setDate(dateStr)
                            endDateViewModel.setDate(null)
                        }
                        clicked == start -> {
                            if (endMinOffsetDaysFromStart <= 0) {
                                endDateViewModel.setDate(null)
                            }
                        }
                        else -> {
                            if (endMinOffsetDaysFromStart > 0) {
                                val minEnd = addDays(start, endMinOffsetDaysFromStart)
                                if (clicked >= minEnd) {
                                    endDateViewModel.setDate(dateStr)
                                }
                            } else {
                                endDateViewModel.setDate(dateStr)
                            }
                        }
                    }
                }
                else -> {
                    startDateViewModel.setDate(dateStr)
                    endDateViewModel.setDate(null)
                }
            }
        }
    }

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
        onClick { closeAll() }
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
            onClick { it.stopPropagation() }
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
                Text("Даты аренды")
            }

            DateRangeCalendarMonth(
                displayMonth = displayMonth,
                minDate = minParsed,
                disabledDates = disabledDates,
                rangeStart = parseIsoLocalDate(startState.date),
                rangeEnd = parseIsoLocalDate(endState.date),
                onMonthChange = { displayMonth = it },
                onDateClick = onDayClick,
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
                        applyTypography(CSSTypography.Styles.button)
                        fontSize(CSSTypography.FontSize.sm)
                        fontWeight(CSSTypography.FontWeight.medium)
                        property("transition", "background-color 0.2s ease")
                    }
                    onClick {
                        if (clearRangeOnCancel) {
                            startDateViewModel.setDate(null)
                            endDateViewModel.setDate(null)
                        }
                        closeAll()
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
                        closeAll()
                        onConfirm?.invoke()
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
                    Text("Готово")
                }
            }
        }
    }
}

@Composable
private fun DateRangeCalendarMonth(
    displayMonth: LocalDate,
    minDate: LocalDate,
    disabledDates: Set<String>,
    rangeStart: LocalDate?,
    rangeEnd: LocalDate?,
    onMonthChange: (LocalDate) -> Unit,
    onDateClick: (LocalDate) -> Unit,
) {
    val firstOfMonth = LocalDate(displayMonth.year, displayMonth.month, 1)
    val startOffset = firstOfMonth.dayOfWeek.ordinal
    val daysInMonth = lastDayOfMonth(displayMonth.year, displayMonth.month)

    val rangeFrom =
        when {
            rangeStart == null -> null
            rangeEnd == null -> rangeStart
            rangeStart <= rangeEnd -> rangeStart
            else -> rangeEnd
        }
    val rangeTo =
        when {
            rangeStart == null -> null
            rangeEnd == null -> rangeStart
            rangeStart <= rangeEnd -> rangeEnd
            else -> rangeStart
        }

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
                onClick { onMonthChange(addMonths(firstOfMonth, -1)) }
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
                Text(monthNames[displayMonth.month.number - 1])
                Span({
                    style {
                        fontWeight(CSSTypography.FontWeight.normal)
                        color(CSSColors.Gray600)
                    }
                }) {
                    Text(" ${displayMonth.year}")
                }
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
                onClick { onMonthChange(addMonths(firstOfMonth, 1)) }
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
            weekDayNames.forEachIndexed { index, name ->
                val weekend = index >= 5
                Div({
                    style {
                        property("width", "calc(100% / 7)")
                        property("min-width", "36px")
                        textAlign("center")
                        applyTypography(CSSTypography.Styles.body)
                        fontSize(CSSTypography.FontSize.xs)
                        color(if (weekend) CSSColors.Red else CSSColors.Gray600)
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
                        height(44.px)
                        property("box-sizing", "border-box")
                    }
                }) {}
            }
            for (day in 1..daysInMonth) {
                val cellDate =
                    LocalDate(
                        displayMonth.year,
                        displayMonth.month,
                        day,
                    )
                val cellDateStr = cellDate.toIsoString()
                val isDisabledDate = disabledDates.contains(cellDateStr)
                val isBeforeMin = cellDate < minDate || isDisabledDate
                val isWeekend =
                    cellDate.dayOfWeek.ordinal == 5 ||
                        cellDate.dayOfWeek.ordinal == 6
                val isRangeStart = rangeStart != null && cellDate == rangeStart
                val isRangeEnd = rangeEnd != null && cellDate == rangeEnd
                val inRange =
                    !isBeforeMin &&
                        rangeFrom != null &&
                        rangeTo != null &&
                        cellDate >= rangeFrom &&
                        cellDate <= rangeTo
                val isSingleAnchor =
                    rangeStart != null &&
                        rangeEnd == null &&
                        cellDate == rangeStart

                Div({
                    style {
                        property("width", "calc(100% / 7)")
                        property("min-width", "36px")
                        height(44.px)
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        justifyContent(JustifyContent.Center)
                        borderRadius(8.px)
                        property("box-sizing", "border-box")
                        when {
                            isBeforeMin -> {
                                applyTypography(CSSTypography.Styles.body)
                                fontSize(CSSTypography.FontSize.sm)
                                color(CSSColors.Gray300)
                                cursor("not-allowed")
                            }
                            inRange && !isRangeStart && !isRangeEnd -> {
                                applyTypography(CSSTypography.Styles.body)
                                fontSize(CSSTypography.FontSize.sm)
                                color(CSSColors.Black)
                                fontWeight(CSSTypography.FontWeight.semibold)
                                property("background-color", "#E8F1FF")
                                cursor("pointer")
                            }
                            else -> {
                                applyTypography(CSSTypography.Styles.body)
                                fontSize(CSSTypography.FontSize.sm)
                                color(
                                    when {
                                        isWeekend && !inRange && !isSingleAnchor -> CSSColors.Red
                                        else -> CSSColors.Black
                                    },
                                )
                                fontWeight(
                                    if (inRange || isSingleAnchor) {
                                        CSSTypography.FontWeight.semibold
                                    } else {
                                        CSSTypography.FontWeight.normal
                                    },
                                )
                                cursor("pointer")
                            }
                        }
                    }
                    onClick {
                        if (!isBeforeMin) {
                            onDateClick(cellDate)
                        }
                    }
                    onMouseEnter {
                        if (!isBeforeMin) {
                            (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                                "background-color",
                                when {
                                    inRange || isRangeStart || isRangeEnd || isSingleAnchor ->
                                        if (inRange && !isRangeStart && !isRangeEnd) {
                                            "#D6E6FF"
                                        } else {
                                            CSSColors.Gray300String
                                        }
                                    else -> CSSColors.Gray300String
                                },
                            )
                        }
                    }
                    onMouseLeave {
                        if (!isBeforeMin) {
                            (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                                "background-color",
                                when {
                                    inRange && !isRangeStart && !isRangeEnd -> "#E8F1FF"
                                    else -> "transparent"
                                },
                            )
                        }
                    }
                }) {
                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Column)
                            alignItems(AlignItems.Center)
                            justifyContent(JustifyContent.Center)
                            gap(2.px)
                        }
                    }) {
                        Text(day.toString())
                        if (!isBeforeMin && (isRangeStart || isRangeEnd || isSingleAnchor)) {
                            Div({
                                style {
                                    width(4.px)
                                    height(4.px)
                                    property("border-radius", "50%")
                                    backgroundColor(CSSColors.Blue)
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}

private fun parseIsoLocalDate(value: String?): LocalDate? =
    value?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

private fun LocalDate.toIsoString(): String =
    "$year-${month.number.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
