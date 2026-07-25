@file:OptIn(kotlin.time.ExperimentalTime::class)
@file:Suppress("ktlint:standard:no-wildcard-imports")

package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import my.drivebit.analytics.reachYandexGoalBron
import my.drivebit.analytics.reachYandexGoalPayAlreadyPaid
import my.drivebit.analytics.reachYandexGoalPayClick
import my.drivebit.analytics.reachYandexGoalPayFail
import my.drivebit.analytics.reachYandexGoalPayRedirect
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.shared.storage.Storage
import my.drivebit.utils.AUTO_BOOK_AFTER_LOGIN
import my.drivebit.utils.END_AT
import my.drivebit.utils.PaymentFunnelKind
import my.drivebit.utils.PaymentFunnelSource
import my.drivebit.utils.RETURN_CAR_ID
import my.drivebit.utils.START_AT
import my.drivebit.utils.addDays
import my.drivebit.utils.encodeUrlParameter
import my.drivebit.utils.getUrlParameter
import my.drivebit.utils.removeUrlQueryParam
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.DateFieldViewModel
import my.drivebit.viewmodels.DateTimeFieldViewModel
import my.drivebit.network.services.BookingCheckoutKind
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
import org.koin.compose.koinInject
import org.w3c.dom.HTMLSelectElement
import kotlin.time.Duration.Companion.hours

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
    val storage: Storage = koinInject()
    val navigationController = LocalNavigationController.current
    val buttonViewModel = createButtonViewModel()
    val payButtonViewModel = createButtonViewModel()
    var lastCarPayKind by remember { mutableStateOf(PaymentFunnelKind.Full) }

    LaunchedEffect(Unit) {
        viewModel.payEffects.collect { effect ->
            when (effect) {
                is RentPayEffect.OpenCheckout -> {
                    reachYandexGoalPayRedirect(
                        source = PaymentFunnelSource.CarDetail,
                        kind = lastCarPayKind,
                        bookingId = effect.bookingId,
                    )
                    window.location.href = effect.url
                }
                is RentPayEffect.ShowInfo -> {
                    if (effect.alreadyPaid) {
                        reachYandexGoalPayAlreadyPaid(
                            source = PaymentFunnelSource.CarDetail,
                            kind = lastCarPayKind,
                            bookingId = effect.bookingId,
                        )
                    } else {
                        reachYandexGoalPayFail(
                            source = PaymentFunnelSource.CarDetail,
                            kind = lastCarPayKind,
                            bookingId = effect.bookingId,
                            message = effect.text,
                        )
                    }
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

    val autoBookAfterLogin =
        remember {
            getUrlParameter(AUTO_BOOK_AFTER_LOGIN).equals("1", ignoreCase = true) ||
                getUrlParameter(AUTO_BOOK_AFTER_LOGIN).equals("true", ignoreCase = true)
        }

    val bookState = state as? RentState.Book ?: return

    var autoBookConsumed by remember { mutableStateOf(false) }

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

    LaunchedEffect(
        autoBookAfterLogin,
        autoBookConsumed,
        bookState.startDate,
        bookState.endDate,
        bookState.showStartDateError,
        bookState.showEndDateError,
        bookState.pendingPaymentBookingId,
        bookState.isCreating,
    ) {
        if (!autoBookAfterLogin || autoBookConsumed) return@LaunchedEffect
        if (bookState.pendingPaymentBookingId != null) return@LaunchedEffect
        if (bookState.isCreating) return@LaunchedEffect
        if (!storage.isLogined()) return@LaunchedEffect
        if (bookState.startDate.isNullOrBlank() || bookState.endDate.isNullOrBlank()) return@LaunchedEffect
        if (bookState.showStartDateError || bookState.showEndDateError) return@LaunchedEffect
        autoBookConsumed = true
        removeUrlQueryParam(AUTO_BOOK_AFTER_LOGIN)
        reachYandexGoalBron()
        viewModel.onBookClick()
    }

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

    val startDateTimeViewModel = remember { DateTimeFieldViewModel() }
    val endDateTimeViewModel = remember { DateTimeFieldViewModel() }
    val bookRangeStartVm = remember { DateFieldViewModel() }
    val bookRangeEndVm = remember { DateFieldViewModel() }

    val startDateTimeState by startDateTimeViewModel.state.collectAsState()
    val endDateTimeState by endDateTimeViewModel.state.collectAsState()
    val bookRangeStartState by bookRangeStartVm.state.collectAsState()
    val bookRangeEndState by bookRangeEndVm.state.collectAsState()

    val rangeCalendarOpen =
        bookRangeStartState.isCalendarOpen || bookRangeEndState.isCalendarOpen

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

    LaunchedEffect(bookState.startDate, bookState.endDate, rangeCalendarOpen) {
        if (rangeCalendarOpen) return@LaunchedEffect
        val sd = bookingInstantToUiDate(bookState.startDate)
        val ed = bookingInstantToUiDate(bookState.endDate)
        if (sd != bookRangeStartState.date) bookRangeStartVm.setDate(sd)
        if (ed != bookRangeEndState.date) bookRangeEndVm.setDate(ed)
    }

    LaunchedEffect(bookRangeStartState.date, startDateTimeState.time) {
        val d = bookRangeStartState.date
        val t = startDateTimeState.time ?: "10:00"
        val target = if (d != null) formatStartAt(d, t) else null
        if (target != bookState.startDate) viewModel.setStartDate(target)
    }

    LaunchedEffect(bookRangeEndState.date, endDateTimeState.time, startDateTimeState.time) {
        val d = bookRangeEndState.date
        val t = endDateTimeState.time ?: startDateTimeState.time ?: "10:00"
        val target = if (d != null) formatEndAt(d, t) else null
        if (target != bookState.endDate) viewModel.setEndDate(target)
    }

    val openBookRangeCalendar: () -> Unit = {
        bookRangeStartVm.setDate(startDateTimeState.date)
        bookRangeEndVm.setDate(endDateTimeState.date)
        bookRangeStartVm.openCalendar()
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
                onOpenDatePicker = openBookRangeCalendar,
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
                onOpenDatePicker = openBookRangeCalendar,
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
                        if (payUi.canPayPrepayment) {
                            ActionButton(
                                viewModel = payButtonViewModel,
                                enabledColor = CSSColors.Blue,
                                text = payUi.prepaymentButtonLabel,
                                onClick = {
                                    lastCarPayKind = PaymentFunnelKind.Prepay
                                    val bookingId = bookState.pendingPaymentBookingId.orEmpty()
                                    reachYandexGoalPayClick(
                                        source = PaymentFunnelSource.CarDetail,
                                        kind = PaymentFunnelKind.Prepay,
                                        bookingId = bookingId,
                                    )
                                    val origin = window.location.origin
                                    viewModel.payCreatedBooking(
                                        kind = BookingCheckoutKind.Prepayment,
                                        returnUrl = "$origin/payment-success",
                                        failUrl = "$origin/payment-failure",
                                    )
                                },
                            )
                        }
                        ActionButton(
                            viewModel = payButtonViewModel,
                            enabledColor = CSSColors.Blue,
                            text = payUi.fullPaymentLabel,
                            onClick = {
                                lastCarPayKind = PaymentFunnelKind.Full
                                val bookingId = bookState.pendingPaymentBookingId.orEmpty()
                                reachYandexGoalPayClick(
                                    source = PaymentFunnelSource.CarDetail,
                                    kind = PaymentFunnelKind.Full,
                                    bookingId = bookingId,
                                )
                                val origin = window.location.origin
                                viewModel.payCreatedBooking(
                                    kind = BookingCheckoutKind.FullOrBalance,
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

    if (rangeCalendarOpen) {
        DateRangeCalendarDialog(
            startDateViewModel = bookRangeStartVm,
            endDateViewModel = bookRangeEndVm,
            minDate =
                Clock.System
                    .now()
                    .toString()
                    .take(10),
            disabledDates = disabledDates,
            endMinOffsetDaysFromStart = 1,
            clearRangeOnCancel = false,
        )
    }
}

@Composable
@Suppress("FunctionName", "UNUSED_PARAMETER")
private fun CarBookDateTimeField(
    label: String,
    actionLabel: String,
    viewModel: DateTimeFieldViewModel,
    minDate: String? = null,
    showError: Boolean,
    enabled: Boolean = true,
    onOpenDatePicker: () -> Unit,
    onDateTimeChanged: (date: String?, time: String?) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val dateText = state.date?.let { formatDateForDisplay(it) } ?: "дата"
    val hourOptions = remember { carBookHourTimeOptionValues() }
    val timeSelectable = enabled && state.date != null
    val visibleHourOptions =
        filterCarBookHourOptionsForSelectedDate(
            selectedDateIso = state.date,
            allHours = hourOptions,
        )

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
                        if (enabled) onOpenDatePicker()
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
                    visibleHourOptions.forEach { value ->
                        Option(
                            value = value,
                            attrs = {
                                if (current == value) selected()
                            },
                        ) {
                            Text(value)
                        }
                    }
                    if (current.isNotEmpty() && current !in visibleHourOptions) {
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

private fun bookingInstantToUiDate(iso: String?): String? =
    iso?.let { s ->
        runCatching {
            kotlinx.datetime.Instant
                .parse(s)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
                .toString()
        }.getOrNull() ?: s.take(10).takeIf { it.length == 10 }
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

private fun carBookOptionHourForFilter(value: String): Int =
    when (value) {
        "24:00" -> 24
        else -> value.substringBefore(':').toIntOrNull() ?: -1
    }

private fun filterCarBookHourOptionsForSelectedDate(
    selectedDateIso: String?,
    allHours: List<String>,
    tz: TimeZone = TimeZone.currentSystemDefault(),
): List<String> {
    val now = Clock.System.now()
    val selected =
        selectedDateIso
            ?.take(10)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: return allHours
    val today = now.toLocalDateTime(tz).date
    if (selected != today) return allHours
    val currentHour = now.toLocalDateTime(tz).hour
    return allHours.filter { carBookOptionHourForFilter(it) > currentHour }
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
