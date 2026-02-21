@file:OptIn(kotlin.time.ExperimentalTime::class)
@file:Suppress("ktlint:standard:no-wildcard-imports")

package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.network.services.CarBookingItem
import my.drivebit.utils.addDays
import my.drivebit.utils.parseDisabledDatesFromBookings
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.DateTimeFieldViewModel
import my.drivebit.viewmodels.RentState
import my.drivebit.viewmodels.RentViewModel
import my.drivebit.viewmodels.createButtonViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import kotlin.time.Duration.Companion.hours

@Composable
@Suppress("FunctionName")
fun CarBook(
    viewModel: RentViewModel,
    carBookings: List<CarBookingItem> = emptyList(),
) {
    val state by viewModel.state.collectAsState()
    val navigationController = LocalNavigationController.current
    val buttonViewModel = createButtonViewModel()

    LaunchedEffect(state) {
        if (state is RentState.NavigateToMyBookings) {
            navigationController?.navigateTo("/my-bookings")
            viewModel.consumeNavigationEvent()
        }
    }

    val bookState = state as? RentState.Book ?: return

    val disabledDates = remember(carBookings) { parseDisabledDatesFromBookings(carBookings) }
    val startDateTimeViewModel = remember { DateTimeFieldViewModel() }
    val endDateTimeViewModel = remember { DateTimeFieldViewModel() }

    LaunchedEffect(bookState.isCreating) {
        buttonViewModel.setState(
            if (bookState.isCreating) ButtonState.Loading else ButtonState.Enabled,
        )
    }
    val startDateTimeState by startDateTimeViewModel.state.collectAsState()
    val endDateTimeState by endDateTimeViewModel.state.collectAsState()

    val endDateMinDate =
        startDateTimeState.date?.take(10)?.let { str ->
            if (str.length == 10) {
                runCatching { addDays(LocalDate.parse(str), 1).toString() }.getOrNull()
            } else {
                null
            }
        }

    LaunchedEffect(bookState.startDate) {
        bookState.startDate?.let { iso ->
            runCatching {
                val instant = kotlinx.datetime.Instant.parse(iso)
                val ldt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                startDateTimeViewModel.setDate(ldt.date.toString())
                startDateTimeViewModel.setTime(
                    "${ldt.hour.toString().padStart(2, '0')}:${ldt.minute.toString().padStart(2, '0')}",
                )
            }.getOrNull() ?: run {
                val datePart = iso.take(10)
                if (datePart.length == 10) {
                    startDateTimeViewModel.setDate(datePart)
                    startDateTimeViewModel.setTime("10:00")
                }
            }
        } ?: run {
            startDateTimeViewModel.setDate(null)
            startDateTimeViewModel.setTime(null)
        }
    }
    LaunchedEffect(bookState.endDate) {
        bookState.endDate?.let { iso ->
            runCatching {
                val instant = kotlinx.datetime.Instant.parse(iso)
                val ldt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                endDateTimeViewModel.setDate(ldt.date.toString())
                endDateTimeViewModel.setTime(
                    "${ldt.hour.toString().padStart(2, '0')}:${ldt.minute.toString().padStart(2, '0')}",
                )
            }.getOrNull() ?: run {
                val datePart = iso.take(10)
                if (datePart.length == 10) {
                    endDateTimeViewModel.setDate(datePart)
                    endDateTimeViewModel.setTime("18:00")
                }
            }
        } ?: run {
            endDateTimeViewModel.setDate(null)
            endDateTimeViewModel.setTime(null)
        }
    }

    Div({
        style {
            flex(1)
            minWidth(280.px)
            property("max-width", "400px")
            padding(20.px)
            borderRadius(12.px)
            border(1.px, LineStyle.Solid, CSSColors.Gray300)
            backgroundColor(CSSColors.White)
            property("box-shadow", "0 2px 8px rgba(0, 0, 0, 0.08)")
        }
    }) {
        Column(gap = 16.px) {
            CarBookDateTimeField(
                label = "Дата начала",
                actionLabel = "c",
                viewModel = startDateTimeViewModel,
                showError = bookState.showStartDateError,
                onDateTimeChanged = { date, time ->
                    viewModel.setStartDate(if (date != null && time != null) formatStartAt(date, time) else null)
                },
            )
            CarBookDateTimeField(
                label = "Дата окончания",
                actionLabel = "по",
                viewModel = endDateTimeViewModel,
                minDate = endDateMinDate,
                showError = bookState.showEndDateError,
                enabled = startDateTimeState.date != null,
                onDateTimeChanged = { date, time ->
                    viewModel.setEndDate(if (date != null && time != null) formatEndAt(date, time) else null)
                },
            )
            if (bookState.totalAmount.isNotEmpty() && bookState.middlePrice.isNotEmpty()) {
                Div({
                    style {
                        paddingTop(8.px)
                        paddingBottom(4.px)
                        property("border-top", "1px solid ${CSSColors.Gray300String}")
                    }
                }) {
                    Span({
                        style {
                            applyTypography(CSSTypography.Styles.body)
                            fontSize(CSSTypography.FontSize.sm)
                            color(CSSColors.Gray600)
                        }
                    }) {
                        Text("${bookState.middlePrice} ₽ / сутки")
                    }
                    Span({
                        style {
                            display(DisplayStyle.Block)
                            applyTypography(CSSTypography.Styles.body)
                            fontSize(CSSTypography.FontSize.xl)
                            fontWeight(CSSTypography.FontWeight.semibold)
                            color(CSSColors.Black)
                            marginTop(4.px)
                        }
                    }) {
                        Text("${bookState.totalAmount} ₽")
                    }
                }
            }
            ActionButton(
                viewModel = buttonViewModel,
                enabledColor = CSSColors.Blue,
                text = "Забронировать",
                onClick = { viewModel.onBookClick() },
            )
            bookState.createError?.let { error ->
                Span({
                    style {
                        applyTypography(CSSTypography.Styles.body)
                        fontSize(CSSTypography.FontSize.xs)
                        color(CSSColors.Red)
                    }
                }) {
                    Text(error)
                }
            }
        }
    }

    if (startDateTimeState.isCalendarOpen) {
        BookingDatePickerForDateTime(
            label = "Дата начала",
            viewModel = startDateTimeViewModel,
            minDate =
                Clock.System
                    .now()
                    .toString()
                    .take(10),
            disabledDates = disabledDates,
            defaultTime = "10:00",
            onDateTimeChanged = { date, time ->
                viewModel.setStartDate(if (date != null && time != null) formatStartAt(date, time) else null)
            },
        )
    }
    if (startDateTimeState.isTimePickerOpen) {
        BookingTimePickerDialog(
            label = "Время начала",
            viewModel = startDateTimeViewModel,
            defaultTime = "10:00",
            onDateTimeChanged = { date, time ->
                viewModel.setStartDate(if (date != null && time != null) formatStartAt(date, time) else null)
            },
        )
    }
    if (endDateTimeState.isCalendarOpen) {
        BookingDatePickerForDateTime(
            label = "Дата окончания",
            viewModel = endDateTimeViewModel,
            minDate = endDateMinDate,
            disabledDates = disabledDates,
            defaultTime = "18:00",
            onDateTimeChanged = { date, time ->
                viewModel.setEndDate(if (date != null && time != null) formatEndAt(date, time) else null)
            },
        )
    }
    if (endDateTimeState.isTimePickerOpen) {
        BookingTimePickerDialog(
            label = "Время окончания",
            viewModel = endDateTimeViewModel,
            defaultTime = "18:00",
            onDateTimeChanged = { date, time ->
                viewModel.setEndDate(if (date != null && time != null) formatEndAt(date, time) else null)
            },
        )
    }
}

@Composable
@Suppress("FunctionName")
private fun CarBookDateTimeField(
    label: String,
    actionLabel: String,
    viewModel: DateTimeFieldViewModel,
    minDate: String? = null,
    showError: Boolean,
    enabled: Boolean = true,
    onDateTimeChanged: (date: String?, time: String?) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val dateText = state.date?.let { formatDateForDisplay(it) } ?: "дата"
    val timeText = state.time ?: "время"

    Div({
        style {
            marginBottom(if (showError) 4.px else 0.px)
            border(1.px, LineStyle.Solid, if (showError) CSSColors.Red else CSSColors.Gray300)
            borderRadius(8.px)
            padding(12.px, 14.px)
            property("transition", "border-color 0.2s ease")
            if (!enabled) {
                property("opacity", "0.6")
                property("pointer-events", "none")
            }
        }
    }) {
        Row(
            alignItems = AlignItems.Center,
            gap = 8.px,
        ) {
            Span({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    fontSize(CSSTypography.FontSize.sm)
                    fontWeight(CSSTypography.FontWeight.medium)
                    color(CSSColors.Blue)
                }
            }) {
                Text(actionLabel)
            }
            Div({
                style {
                    flex(1)
                    display(DisplayStyle.Flex)
                    gap(8.px)
                    flexWrap(FlexWrap.Wrap)
                }
            }) {
                Div({
                    style {
                        cursor(if (enabled) "pointer" else "not-allowed")
                        padding(4.px, 8.px)
                        borderRadius(6.px)
                        property("transition", "background-color 0.2s ease")
                    }
                    onClick {
                        if (enabled) viewModel.openCalendar()
                    }
                    onMouseEnter {
                        if (enabled) {
                            (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                                "background-color",
                                CSSColors.Gray300String,
                            )
                        }
                    }
                    onMouseLeave {
                        (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                            "background-color",
                            "transparent",
                        )
                    }
                }) {
                    Row(alignItems = AlignItems.Center, gap = 4.px) {
                        Span({
                            style {
                                applyTypography(CSSTypography.Styles.body)
                                fontSize(CSSTypography.FontSize.base)
                                fontWeight(CSSTypography.FontWeight.medium)
                                color(if (state.date != null) CSSColors.Black else CSSColors.Gray600)
                            }
                        }) {
                            Text(dateText)
                        }
                        Img(
                            src = "/images/arrow-bottom.svg",
                            alt = "",
                            attrs = {
                                style {
                                    width(12.px)
                                    height(8.px)
                                }
                            },
                        )
                    }
                }
                Div({
                    style {
                        cursor(if (enabled && state.date != null) "pointer" else "not-allowed")
                        padding(4.px, 8.px)
                        borderRadius(6.px)
                        property("transition", "background-color 0.2s ease")
                        if (state.date == null) property("opacity", "0.6")
                    }
                    onClick {
                        if (enabled && state.date != null) viewModel.openTimePicker()
                    }
                    onMouseEnter {
                        if (enabled && state.date != null) {
                            (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                                "background-color",
                                CSSColors.Gray300String,
                            )
                        }
                    }
                    onMouseLeave {
                        (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                            "background-color",
                            "transparent",
                        )
                    }
                }) {
                    Row(alignItems = AlignItems.Center, gap = 4.px) {
                        Span({
                            style {
                                applyTypography(CSSTypography.Styles.body)
                                fontSize(CSSTypography.FontSize.base)
                                fontWeight(CSSTypography.FontWeight.medium)
                                color(if (state.time != null) CSSColors.Black else CSSColors.Gray600)
                            }
                        }) {
                            Text(timeText)
                        }
                        Img(
                            src = "/images/arrow-bottom.svg",
                            alt = "",
                            attrs = {
                                style {
                                    width(12.px)
                                    height(8.px)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
    if (showError) {
        Span({
            style {
                applyTypography(CSSTypography.Styles.body)
                fontSize(CSSTypography.FontSize.xs)
                color(CSSColors.Red)
            }
        }) {
            Text("Выберите дату и время")
        }
    }
}

private fun formatStartAt(date: String, time: String): String {
    val selectedDate = LocalDate.parse(date.take(10))
    val today =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
    val localDateTime = LocalDateTime.parse("${date.take(10)}T${time}:00")
    val instant = localDateTime.toInstant(TimeZone.currentSystemDefault())
    return if (selectedDate == today) {
        val now = Clock.System.now()
        if (instant.epochSeconds <= now.epochSeconds) (now + 1.hours).toString() else instant.toString()
    } else {
        instant.toString()
    }
}

private fun formatEndAt(date: String, time: String): String {
    val localDateTime = LocalDateTime.parse("${date.take(10)}T${time}:00")
    return localDateTime.toInstant(TimeZone.currentSystemDefault()).toString()
}

private fun formatDateForDisplay(dateString: String): String {
    if (dateString.length < 10) return dateString
    val parts = dateString.split("-")
    if (parts.size != 3) return dateString
    return "${parts[2]}.${parts[1]}.${parts[0]}"
}
