@file:OptIn(kotlin.time.ExperimentalTime::class)
@file:Suppress("ktlint:standard:no-wildcard-imports")

package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import my.drivebit.analytics.reachYandexGoalBron
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.utils.END_AT
import my.drivebit.utils.RETURN_CAR_ID
import my.drivebit.utils.START_AT
import my.drivebit.utils.addDays
import my.drivebit.utils.encodeUrlParameter
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.DateTimeFieldViewModel
import my.drivebit.viewmodels.PendingBookingPaymentUi
import my.drivebit.viewmodels.RentPayEffect
import my.drivebit.viewmodels.RentState
import my.drivebit.viewmodels.RentViewModel
import my.drivebit.viewmodels.createButtonViewModel
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.attributes.selected
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Select
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import kotlin.time.Duration.Companion.hours
import org.w3c.dom.HTMLSelectElement

@Composable
@Suppress("FunctionName")
fun CarBook(
    viewModel: RentViewModel,
    disabledDates: Set<String> = emptySet(),
    initialStartAt: String? = null,
    initialEndAt: String? = null,
) {
    val state by viewModel.state.collectAsState()
    val isPaying by viewModel.isPaying.collectAsState()
    val navigationController = LocalNavigationController.current
    val buttonViewModel = createButtonViewModel()
    val payButtonViewModel = createButtonViewModel()

    LaunchedEffect(Unit) {
        viewModel.payEffects.collect { effect ->
            when (effect) {
                is RentPayEffect.OpenCheckout -> {
                    window.location.href = effect.url
                }
                is RentPayEffect.ShowInfo -> {
                    window.alert(effect.text)
                }
            }
        }
    }

    LaunchedEffect(state) {
        when (state) {
            is RentState.NavigateToMyBookings -> {
                navigationController?.navigateTo("/my-bookings")
                viewModel.consumeNavigationEvent()
            }
            is RentState.NavigateToLogin -> {
                val loginState = state as RentState.NavigateToLogin
                val params = mutableListOf("$RETURN_CAR_ID=${loginState.carId.encodeUrlParameter()}")
                loginState.startDate?.takeIf { it.isNotBlank() }?.let {
                    params.add("$START_AT=${it.encodeUrlParameter()}")
                }
                loginState.endDate?.takeIf { it.isNotBlank() }?.let {
                    params.add("$END_AT=${it.encodeUrlParameter()}")
                }
                val query = params.joinToString("&")
                navigationController?.navigateTo("/login-by-phone?$query")
                viewModel.consumeNavigationEvent()
            }
            else -> {}
        }
    }

    val bookState = state as? RentState.Book ?: return

    LaunchedEffect(initialStartAt, initialEndAt) {
        when {
            initialStartAt != null && initialEndAt != null -> {
                // Если стартовая дата = "сегодня" и время уже в прошлом,
                // бэкенд возвращает conflict: "Дата начала бронирования должна быть позже текущего времени."
                // Поэтому корректируем startAt на (now + 1h) перед расчетом.
                val tz = TimeZone.currentSystemDefault()
                val nowInstant = Clock.System.now()

                val startInstant = kotlinx.datetime.Instant.parse(initialStartAt)
                val startLdt = startInstant.toLocalDateTime(tz)
                val today = nowInstant.toLocalDateTime(tz).date

                val adjustedStartInstant =
                    if (startLdt.date == today && startInstant.epochSeconds <= nowInstant.epochSeconds) {
                        nowInstant + 1.hours
                    } else {
                        startInstant
                    }

                val adjustedStartAt = adjustedStartInstant.toString()

                val adjustedEndAt =
                    if (adjustedStartInstant != startInstant) {
                        val endInstant = kotlinx.datetime.Instant.parse(initialEndAt)
                        val endLdt = endInstant.toLocalDateTime(tz)
                        val endTimeWasDefault = endLdt.hour == 10 && endLdt.minute == 0

                        // Если endAt пришёл "только с датами" (значение по умолчанию 10:00),
                        // подтягиваем время конца к времени начала.
                        if (endTimeWasDefault) {
                            val adjustedStartLdt = adjustedStartInstant.toLocalDateTime(tz)
                            val newEndLdt = LocalDateTime(endLdt.date, adjustedStartLdt.time)
                            newEndLdt.toInstant(tz).toString()
                        } else {
                            initialEndAt
                        }
                    } else {
                        initialEndAt
                    }

                viewModel.setInitialDates(adjustedStartAt, adjustedEndAt)
            }
            else -> {
                initialStartAt?.let { viewModel.setStartDate(it) }
                initialEndAt?.let { viewModel.setEndDate(it) }
            }
        }
    }

    val startDateTimeViewModel = remember { DateTimeFieldViewModel() }
    val endDateTimeViewModel = remember { DateTimeFieldViewModel() }

    LaunchedEffect(bookState.isCreating, bookState.pendingPaymentBookingId) {
        buttonViewModel.setState(
            when {
                bookState.isCreating -> ButtonState.Loading
                bookState.pendingPaymentBookingId != null -> ButtonState.Disabled
                else -> ButtonState.Enabled
            },
        )
    }

    LaunchedEffect(bookState.startDate, bookState.endDate) {
        viewModel.refreshPendingBookingForCurrentSelection()
    }

    LaunchedEffect(bookState.pendingBookingPaymentUi) {
        if (bookState.pendingBookingPaymentUi is PendingBookingPaymentUi.AwaitingOwnerConfirmation) {
            while (true) {
                delay(15_000)
                viewModel.refreshPendingBookingForCurrentSelection()
            }
        }
    }

    LaunchedEffect(isPaying, bookState.pendingBookingPaymentUi) {
        payButtonViewModel.setState(
            when {
                isPaying -> ButtonState.Loading
                bookState.pendingBookingPaymentUi is PendingBookingPaymentUi.ReadyToPay -> ButtonState.Enabled
                else -> ButtonState.Disabled
            },
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
    LaunchedEffect(bookState.endDate, startDateTimeState.time) {
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
                    endDateTimeViewModel.setTime(startDateTimeState.time ?: "10:00")
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
                    if (bookState.depositAmount.isNotEmpty()) {
                        Span({
                            style {
                                display(DisplayStyle.Block)
                                applyTypography(CSSTypography.Styles.body)
                                fontSize(CSSTypography.FontSize.sm)
                                color(CSSColors.Gray600)
                                marginTop(2.px)
                            }
                        }) {
                            Text("Депозит: ${bookState.depositAmount} ₽")
                        }
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
                onClick = {
                    reachYandexGoalBron()
                    viewModel.onBookClick()
                },
            )
            if (bookState.pendingPaymentBookingId != null) {
                when (val payUi = bookState.pendingBookingPaymentUi) {
                    is PendingBookingPaymentUi.AwaitingOwnerConfirmation -> {
                        Span({
                            style {
                                display(DisplayStyle.Block)
                                applyTypography(CSSTypography.Styles.body)
                                fontSize(CSSTypography.FontSize.sm)
                                color(CSSColors.Gray600)
                                marginTop((-4).px)
                            }
                        }) {
                            Text("Бронирование создано. Ожидается подтверждение брони: ${payUi.statusLabel}.")
                        }
                    }
                    is PendingBookingPaymentUi.ReadyToPay -> {
                        Span({
                            style {
                                display(DisplayStyle.Block)
                                applyTypography(CSSTypography.Styles.body)
                                fontSize(CSSTypography.FontSize.sm)
                                color(CSSColors.Gray600)
                                marginTop((-4).px)
                            }
                        }) {
                            Text("Бронирование подтверждено. Оплатите, чтобы продолжить.")
                        }
                        ActionButton(
                            viewModel = payButtonViewModel,
                            enabledColor = CSSColors.Blue,
                            text = "Оплатить",
                            onClick = {
                                val origin = window.location.origin
                                viewModel.payCreatedBooking(
                                    returnUrl = "$origin/payment-success",
                                    failUrl = "$origin/payment-failure",
                                )
                            },
                        )
                    }
                    null -> {
                        Span({
                            style {
                                display(DisplayStyle.Block)
                                applyTypography(CSSTypography.Styles.body)
                                fontSize(CSSTypography.FontSize.sm)
                                color(CSSColors.Gray600)
                                marginTop((-4).px)
                            }
                        }) {
                            Text("Бронирование создано. Ожидается подтверждение брони.")
                        }
                    }
                }
                Div({
                    style {
                        width(100.percent)
                        textAlign("center")
                        marginTop(4.px)
                    }
                    onClick {
                        viewModel.requestNavigateToMyBookings()
                    }
                }) {
                    Span({
                        style {
                            applyTypography(CSSTypography.Styles.body)
                            fontSize(CSSTypography.FontSize.sm)
                            color(CSSColors.Blue)
                            property("cursor", "pointer")
                            property("text-decoration", "underline")
                        }
                    }) {
                        Text("Мои бронирования")
                    }
                }
            }
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
    if (endDateTimeState.isCalendarOpen) {
        BookingDatePickerForDateTime(
            label = "Дата окончания",
            viewModel = endDateTimeViewModel,
            minDate = endDateMinDate,
            disabledDates = disabledDates,
            defaultTime = startDateTimeState.time ?: "10:00",
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
    val hourOptions = remember { carBookHourTimeOptionValues() }
    val timeSelectable = enabled && state.date != null

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
                Select(
                    attrs = {
                        if (!timeSelectable) {
                            disabled()
                        }
                        onChange { event ->
                            val v = (event.target as HTMLSelectElement).value
                            val picked = v.takeIf { it.isNotBlank() }
                            viewModel.setTime(picked)
                            onDateTimeChanged(state.date, picked)
                        }
                        style {
                            minWidth(108.px)
                            padding(6.px, 8.px)
                            borderRadius(6.px)
                            border(1.px, LineStyle.Solid, CSSColors.Gray300)
                            applyTypography(CSSTypography.Styles.body)
                            fontSize(CSSTypography.FontSize.sm)
                            color(CSSColors.Black)
                            property("background-color", CSSColors.WhiteString)
                            cursor(if (timeSelectable) "pointer" else "not-allowed")
                            if (!timeSelectable) property("opacity", "0.6")
                        }
                    },
                ) {
                    val current = state.time.orEmpty()
                    Option(
                        value = "",
                        attrs = {
                            if (current.isEmpty()) selected()
                        },
                    ) {
                        Text("Час")
                    }
                    hourOptions.forEach { value ->
                        Option(
                            value = value,
                            attrs = {
                                if (current == value) selected()
                            },
                        ) {
                            Text(value)
                        }
                    }
                    if (current.isNotEmpty() && current !in hourOptions) {
                        Option(
                            value = current,
                            attrs = { selected() },
                        ) {
                            Text(current)
                        }
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

private fun formatStartAt(
    date: String,
    time: String,
): String {
    val selectedDate = LocalDate.parse(date.take(10))
    val today =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
    val localDateTime = localDateTimeFromDateAndTime(date, time)
    val instant = localDateTime.toInstant(TimeZone.currentSystemDefault())
    return if (selectedDate == today) {
        val now = Clock.System.now()
        if (instant.epochSeconds <= now.epochSeconds) (now + 1.hours).toString() else instant.toString()
    } else {
        instant.toString()
    }
}

private fun formatEndAt(
    date: String,
    time: String,
): String {
    val localDateTime = localDateTimeFromDateAndTime(date, time)
    return localDateTime.toInstant(TimeZone.currentSystemDefault()).toString()
}

private fun carBookHourTimeOptionValues(): List<String> =
    (0..24).map { h ->
        if (h == 24) {
            "24:00"
        } else {
            "${h.toString().padStart(2, '0')}:00"
        }
    }

private fun localDateTimeFromDateAndTime(
    date: String,
    time: String,
): LocalDateTime {
    val d = LocalDate.parse(date.take(10))
    val t = time.trim()
    if (t == "24:00" || t.startsWith("24:")) {
        return LocalDateTime(d.year, d.month, d.day, 23, 59, 0, 0)
    }
    val parts = t.split(':')
    val hh = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val mm = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return LocalDateTime(d.year, d.month, d.day, hh, mm, 0, 0)
}

private fun formatDateForDisplay(dateString: String): String {
    if (dateString.length < 10) return dateString
    val parts = dateString.split("-")
    if (parts.size != 3) return dateString
    return "${parts[2]}.${parts[1]}.${parts[0]}"
}
