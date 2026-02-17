package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.datetime.Clock
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.DateFieldViewModel
import my.drivebit.viewmodels.RentState
import my.drivebit.viewmodels.RentViewModel
import my.drivebit.viewmodels.createButtonViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
@Suppress("FunctionName")
fun CarBook(viewModel: RentViewModel) {
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

    val startDateViewModel = remember { DateFieldViewModel() }
    val endDateViewModel = remember { DateFieldViewModel() }

    LaunchedEffect(bookState.isCreating) {
        buttonViewModel.setState(
            if (bookState.isCreating) ButtonState.Loading else ButtonState.Enabled,
        )
    }
    val startDateState by startDateViewModel.state.collectAsState()
    val endDateState by endDateViewModel.state.collectAsState()

    LaunchedEffect(bookState.startDate) {
        bookState.startDate?.let { iso ->
            val datePart = iso.take(10)
            if (datePart.length == 10) startDateViewModel.setDate(datePart)
        }
    }
    LaunchedEffect(bookState.endDate) {
        bookState.endDate?.let { iso ->
            val datePart = iso.take(10)
            if (datePart.length == 10) endDateViewModel.setDate(datePart)
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
            CarBookDateField(
                label = "Дата начала",
                actionLabel = "c",
                viewModel = startDateViewModel,
                showError = bookState.showStartDateError,
                onDateChanged = { date ->
                    viewModel.setStartDate(if (date != null) "${date}T10:00:00Z" else null)
                },
            )
            CarBookDateField(
                label = "Дата окончания",
                actionLabel = "по",
                viewModel = endDateViewModel,
                minDate = startDateState.date,
                showError = bookState.showEndDateError,
                onDateChanged = { date ->
                    viewModel.setEndDate(if (date != null) "${date}T18:00:00Z" else null)
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

    if (startDateState.isCalendarOpen) {
        DateFieldDialog(
            label = "Дата начала",
            viewModel = startDateViewModel,
            minDate =
                Clock.System
                    .now()
                    .toString()
                    .take(10),
            onDateChanged = { date ->
                viewModel.setStartDate(if (date != null) "${date}T10:00:00Z" else null)
            },
        )
    }
    if (endDateState.isCalendarOpen) {
        DateFieldDialog(
            label = "Дата окончания",
            viewModel = endDateViewModel,
            minDate = startDateState.date,
            onDateChanged = { date ->
                viewModel.setEndDate(if (date != null) "${date}T18:00:00Z" else null)
            },
        )
    }
}

@Composable
private fun CarBookDateField(
    label: String,
    actionLabel: String,
    viewModel: DateFieldViewModel,
    minDate: String? = null,
    showError: Boolean,
    onDateChanged: (String?) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val currentDate = state.date?.let { formatDateForDisplay(it) } ?: "выберите даты"

    Div({
        style {
            cursor("pointer")
            marginBottom(if (showError) 4.px else 0.px)
            border(1.px, LineStyle.Solid, if (showError) CSSColors.Red else CSSColors.Gray300)
            borderRadius(8.px)
            padding(12.px, 14.px)
            property("transition", "border-color 0.2s ease")
        }
        onClick {
            viewModel.openCalendar()
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
            Span({
                style {
                    flex(1)
                    applyTypography(CSSTypography.Styles.body)
                    fontSize(CSSTypography.FontSize.base)
                    fontWeight(CSSTypography.FontWeight.medium)
                    color(if (state.date != null) CSSColors.Black else CSSColors.Gray600)
                }
            }) {
                Text(currentDate)
            }
            Img(
                src = "/images/arrow-bottom.svg",
                alt = "",
                attrs = {
                    style {
                        width(16.px)
                        height(10.px)
                    }
                },
            )
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
            Text("Выберите дату")
        }
    }
}

private fun formatDateForDisplay(dateString: String): String {
    if (dateString.length < 10) return dateString
    val parts = dateString.split("-")
    if (parts.size != 3) return dateString
    return "${parts[2]}.${parts[1]}.${parts[0]}"
}
