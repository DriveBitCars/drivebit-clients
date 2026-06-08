package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.Column
import my.drivebit.components.FormSection
import my.drivebit.components.Loader
import my.drivebit.components.PageHeader
import my.drivebit.shell.PageWithLogo
import my.drivebit.components.Row
import my.drivebit.components.TextError
import my.drivebit.components.TextSmallBodyGray
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.network.services.BookingDTO
import my.drivebit.network.services.canShowSignContractAsOwner
import my.drivebit.network.services.statusAllowsContractDownload
import my.drivebit.utils.formatRelativeTime
import my.drivebit.utils.mapIso8601ToDateString
import my.drivebit.utils.mapIso8601ToTimeString
import my.drivebit.viewmodels.MyBookingsAsOwnerViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

@Composable
fun MyDealsPage() {
    val viewModel: MyBookingsAsOwnerViewModel = koinInject()
    val navigationController = LocalNavigationController.current
    val bookings by viewModel.bookings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val actionInProgress by viewModel.actionInProgress.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBookings()
        while (true) {
            delay(30_000)
            viewModel.refreshBookings()
        }
    }

    PageWithLogo {
        CenteredFormContainer(maxWidth = 600.px) {
            PageHeader {
                TextSmartHeader("Мои сделки (${bookings.size})")
            }

            FormSection {
                when {
                    isLoading && bookings.isEmpty() -> Loader()
                    bookings.isEmpty() && error != null -> TextError(error ?: "Ошибка")
                    bookings.isEmpty() -> {
                        Div({
                            style {
                                textAlign("center")
                                padding(32.px)
                                color(CSSColors.Gray600)
                            }
                        }) {
                            Text("У вас пока нет заявок на аренду")
                        }
                    }
                    else -> {
                        Column(gap = 16.px, modifier = { width(100.percent) }) {
                            if (error != null) {
                                TextError(error ?: "Ошибка")
                            }
                            bookings.forEach { booking ->
                                DealItemCard(
                                    booking = booking,
                                    isActionInProgress = booking.id in actionInProgress,
                                    onConfirm = { viewModel.confirmBooking(booking.id) },
                                    onDecline = { viewModel.declineBooking(booking.id) },
                                    onSignContract = { viewModel.signContractAsOwner(booking.id) },
                                    onDownloadContract = {
                                        navigationController?.navigateTo(
                                            "/download-booking-contract?bookingId=${booking.id}",
                                        )
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

@Composable
private fun DealItemCard(
    booking: BookingDTO,
    isActionInProgress: Boolean,
    onConfirm: () -> Unit,
    onDecline: () -> Unit,
    onSignContract: () -> Unit,
    onDownloadContract: () -> Unit,
) {
    val renterName = booking.renterName?.takeIf { it.isNotBlank() } ?: "Арендатор"
    val carName =
        listOfNotNull(booking.carBrandName, booking.carModelName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { "Автомобиль" }
    val shortId = booking.id.takeLast(6)
    val periodStart =
        runCatching {
            "${mapIso8601ToDateString(booking.startAt)} ${mapIso8601ToTimeString(booking.startAt)}"
        }.getOrElse { booking.startAt.take(16) }
    val periodEnd =
        runCatching {
            "${mapIso8601ToDateString(booking.endAt)} ${mapIso8601ToTimeString(booking.endAt)}"
        }.getOrElse { booking.endAt.take(16) }
    val statusDateTime =
        runCatching {
            "${mapIso8601ToDateString(booking.createdAt)}, в ${mapIso8601ToTimeString(booking.createdAt)}"
        }.getOrElse { booking.createdAt }
    val relativeTime = runCatching { formatRelativeTime(Instant.parse(booking.createdAt)) }.getOrElse { "" }
    val canConfirmOrDecline =
        booking.status.equals("Pending", ignoreCase = true) ||
            booking.status.equals("AwaitingOwnerConfirmation", ignoreCase = true)
    val canDownloadContract = booking.statusAllowsContractDownload()
    val canSignContract = booking.canShowSignContractAsOwner()

    Column(
        gap = 16.px,
        modifier = {
            width(100.percent)
            property("box-sizing", "border-box")
            padding(16.px)
            borderRadius(12.px)
            border(1.px, LineStyle.Solid, CSSColors.Gray300)
        },
    ) {
        Row(
            gap = 12.px,
            alignItems = AlignItems.Center,
            modifier = { width(100.percent) },
        ) {
            DealAvatar(name = renterName)
            Column(gap = 4.px) {
                Row(
                    gap = 8.px,
                    alignItems = AlignItems.Center,
                    modifier = { width(100.percent) },
                ) {
                    Span({
                        style {
                            fontSize(16.px)
                            fontWeight("600")
                            color(CSSColors.Black)
                        }
                    }) {
                        Text(renterName)
                    }
                    DealStatusTag(text = booking.statusTranslate?.takeIf { it.isNotBlank() } ?: booking.status)
                }
                Span({
                    style {
                        fontSize(14.px)
                        color(CSSColors.Gray600)
                    }
                }) {
                    Text(carName)
                }
                Span({
                    style {
                        fontSize(13.px)
                        color(CSSColors.Gray600)
                    }
                }) {
                    Text("#$shortId")
                }
            }
        }

        Column(gap = 8.px) {
            TextSmallBodyGray("ПЕРИОД АРЕНДЫ")
            Span({
                style {
                    fontSize(14.px)
                    color(CSSColors.Black)
                }
            }) {
                Text("$periodStart - $periodEnd")
            }
        }

        Column(gap = 8.px) {
            TextSmallBodyGray("СТАТУС СДЕЛКИ")
            Span({
                style {
                    fontSize(14.px)
                    color(CSSColors.Black)
                }
            }) {
                Text("${booking.statusTranslate?.takeIf { it.isNotBlank() } ?: booking.status}: $statusDateTime")
            }
        }

        if (canDownloadContract || canSignContract) {
            Row(
                gap = 8.px,
                modifier = { width(100.percent) },
            ) {
                if (canDownloadContract) {
                    Button({
                        style {
                            padding(8.px, 16.px)
                            backgroundColor(CSSColors.Blue)
                            color(CSSColors.White)
                            border(0.px)
                            borderRadius(8.px)
                            fontSize(14.px)
                            fontWeight("600")
                            cursor("pointer")
                        }
                        onClick { onDownloadContract() }
                    }) {
                        Text("Договор")
                    }
                }
                if (canSignContract) {
                    Button({
                        style {
                            padding(8.px, 16.px)
                            backgroundColor(CSSColors.Blue)
                            color(CSSColors.White)
                            border(0.px)
                            borderRadius(8.px)
                            fontSize(14.px)
                            fontWeight("600")
                            cursor(if (isActionInProgress) "default" else "pointer")
                            property("opacity", if (isActionInProgress) "0.6" else "1")
                        }
                        if (!isActionInProgress) {
                            onClick { onSignContract() }
                        }
                    }) {
                        Text(if (isActionInProgress) "Подписание..." else "Подписать договор")
                    }
                }
            }
        }

        Row(
            justifyContent = JustifyContent.SpaceBetween,
            alignItems = AlignItems.Center,
            modifier = { width(100.percent) },
        ) {
            Span({
                style {
                    fontSize(13.px)
                    color(CSSColors.Gray600)
                }
            }) {
                Text(relativeTime)
            }
            if (canConfirmOrDecline && !isActionInProgress) {
                Row(gap = 8.px) {
                    Button({
                        style {
                            padding(8.px, 16.px)
                            backgroundColor(CSSColors.Blue)
                            color(CSSColors.White)
                            border(0.px)
                            borderRadius(6.px)
                            cursor("pointer")
                            fontSize(14.px)
                            fontWeight("600")
                        }
                        onClick { onConfirm() }
                    }) {
                        Text("Подтвердить")
                    }
                    Button({
                        style {
                            padding(8.px, 16.px)
                            backgroundColor(CSSColors.Gray600)
                            color(CSSColors.White)
                            border(0.px)
                            borderRadius(6.px)
                            cursor("pointer")
                            fontSize(14.px)
                            fontWeight("600")
                        }
                        onClick { onDecline() }
                    }) {
                        Text("Отклонить")
                    }
                }
            }
            if (canConfirmOrDecline && isActionInProgress) {
                Span({
                    style {
                        fontSize(14.px)
                        color(CSSColors.Gray600)
                    }
                }) {
                    Text("Обработка...")
                }
            }
        }
    }
}

@Composable
private fun DealAvatar(name: String) {
    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Div({
        style {
            width(48.px)
            height(48.px)
            borderRadius(50.percent)
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.Center)
            backgroundColor(CSSColors.Gray300)
            color(CSSColors.Gray600)
            fontSize(18.px)
            fontWeight("700")
        }
    }) {
        Text(initial)
    }
}

@Composable
private fun DealStatusTag(text: String) {
    val displayText = text.uppercase()
    Span({
        style {
            padding(4.px, 8.px)
            borderRadius(6.px)
            backgroundColor(CSSColors.Gray300)
            color(CSSColors.Gray600)
            fontSize(12.px)
            fontWeight("600")
        }
    }) {
        Text(displayText)
    }
}
