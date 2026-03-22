package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.datetime.Instant
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.Column
import my.drivebit.components.FormSection
import my.drivebit.components.Loader
import my.drivebit.components.PageHeader
import my.drivebit.components.PageWithLogo
import my.drivebit.components.Row
import my.drivebit.components.TextError
import my.drivebit.components.TextSmallBodyGray
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.network.services.BookingDTO
import my.drivebit.utils.formatRelativeTime
import my.drivebit.utils.mapIso8601ToDateString
import my.drivebit.utils.mapIso8601ToTimeString
import my.drivebit.viewmodels.MyBookingsAsRenterViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

@Composable
fun MyBookingsPage() {
    val viewModel: MyBookingsAsRenterViewModel = koinInject()
    val navigationController = LocalNavigationController.current
    val bookings by viewModel.bookings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBookings()
    }

    PageWithLogo {
        CenteredFormContainer(maxWidth = 600.px) {
            PageHeader {
                TextSmartHeader("Все заявки (${bookings.size})")
            }

            FormSection {
                when {
                    isLoading -> Loader()
                    error != null -> TextError(error ?: "Ошибка")
                    bookings.isEmpty() -> {
                        Div({
                            style {
                                textAlign("center")
                                padding(32.px)
                                color(CSSColors.Gray600)
                            }
                        }) {
                            Text("У вас пока нет бронирований")
                        }
                    }
                    else -> {
                        Column(gap = 16.px, modifier = { width(100.percent) }) {
                            bookings.forEach { booking ->
                                BookingItemCard(
                                    booking = booking,
                                    onLeaveReview = {
                                        navigationController?.navigateTo("/leave-review?carId=${booking.carId}")
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
private fun BookingItemCard(
    booking: BookingDTO,
    onLeaveReview: () -> Unit,
) {
    val ownerName = booking.ownerName?.takeIf { it.isNotBlank() } ?: "Владелец"
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
    val canLeaveReview = booking.status.equals("Completed", ignoreCase = true)

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
            BookingAvatar(name = ownerName)
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
                        Text(ownerName)
                    }
                    StatusTag(text = booking.statusTranslate?.takeIf { it.isNotBlank() } ?: booking.status)
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

        if (canLeaveReview) {
            Row(
                justifyContent = JustifyContent.FlexStart,
                modifier = { width(100.percent) },
            ) {
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
                    onClick { onLeaveReview() }
                }) {
                    Text("Оставить отзыв")
                }
            }
        }

        Row(
            justifyContent = JustifyContent.FlexEnd,
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
        }
    }
}

@Composable
private fun BookingAvatar(name: String) {
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
private fun StatusTag(text: String) {
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
