package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.viewmodels.DateFieldViewModel
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flex
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.minWidth
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement

@Composable
fun SearchDateRangeSelector(
    startDate: String?,
    endDate: String?,
    onStartDateChanged: (String?) -> Unit,
    onEndDateChanged: (String?) -> Unit,
    disabledDates: Set<String> = emptySet(),
    endMinOffsetDaysFromStart: Int = 0,
    clearRangeOnCancel: Boolean = true,
) {
    val startViewModel = remember { DateFieldViewModel(initialDate = startDate) }
    val endViewModel = remember { DateFieldViewModel(initialDate = endDate) }
    val startState by startViewModel.state.collectAsState()
    val endState by endViewModel.state.collectAsState()

    LaunchedEffect(startDate) {
        if (startState.date != startDate) startViewModel.setDate(startDate)
    }
    LaunchedEffect(endDate) {
        if (endState.date != endDate) endViewModel.setDate(endDate)
    }

    LaunchedEffect(startState.date) {
        if (startState.date != startDate) onStartDateChanged(startState.date)
    }
    LaunchedEffect(endState.date) {
        if (endState.date != endDate) onEndDateChanged(endState.date)
    }

    Row(
        gap = 12.px,
        modifier = {
            width(100.percent)
            property("box-sizing", "border-box")
            property("min-width", "0")
        },
    ) {
        SearchDateFieldItem(
            actionLabel = "с",
            value = startState.date?.let(::formatSearchDateForDisplay) ?: "выберите даты",
            isPlaceholder = startState.date == null,
            onClick = { startViewModel.openCalendar() },
        )
        SearchDateFieldItem(
            actionLabel = "по",
            value = endState.date?.let(::formatSearchDateForDisplay) ?: "выберите даты",
            isPlaceholder = endState.date == null,
            onClick = { endViewModel.openCalendar() },
        )
    }

    if (startState.isCalendarOpen || endState.isCalendarOpen) {
        DateRangeCalendarDialog(
            startDateViewModel = startViewModel,
            endDateViewModel = endViewModel,
            disabledDates = disabledDates,
            endMinOffsetDaysFromStart = endMinOffsetDaysFromStart,
            clearRangeOnCancel = clearRangeOnCancel,
        )
    }
}

@Composable
private fun SearchDateFieldItem(
    actionLabel: String,
    value: String,
    isPlaceholder: Boolean,
    onClick: () -> Unit,
) {
    Div({
        style {
            flex(1)
            minWidth(0.px)
            cursor("pointer")
            marginBottom(12.px)
            border(1.px, LineStyle.Solid, CSSColors.Gray300)
            borderRadius(8.px)
            padding(12.px, 14.px)
            property("box-sizing", "border-box")
            property("transition", "border-color 0.2s ease")
        }
        onClick { onClick() }
        onMouseEnter {
            (it.currentTarget as? HTMLElement)?.style?.setProperty(
                "border-color",
                CSSColors.BlueString,
            )
        }
        onMouseLeave {
            (it.currentTarget as? HTMLElement)?.style?.setProperty(
                "border-color",
                CSSColors.Gray300String,
            )
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.SpaceBetween)
                gap(8.px)
            }
        }) {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(8.px)
                    minWidth(0.px)
                    flex(1)
                }
            }) {
                Span({
                    style {
                        applyTypography(CSSTypography.Styles.body)
                        fontSize(CSSTypography.FontSize.base)
                        fontWeight(CSSTypography.FontWeight.medium)
                        color(CSSColors.Blue)
                    }
                }) { Text(actionLabel) }
                Span({
                    style {
                        applyTypography(CSSTypography.Styles.body)
                        fontSize(CSSTypography.FontSize.base)
                        fontWeight(CSSTypography.FontWeight.medium)
                        color(if (isPlaceholder) CSSColors.Gray600 else CSSColors.Black)
                        property("white-space", "nowrap")
                        property("overflow", "hidden")
                        property("text-overflow", "ellipsis")
                    }
                }) { Text(value) }
            }
            CalendarIconBlue(size = 18.px)
        }
    }
}

private fun formatSearchDateForDisplay(dateString: String): String {
    if (dateString.isEmpty()) return ""
    return try {
        val date = LocalDate.parse(dateString.take(10))
        val day = date.day.toString().padStart(2, '0')
        val month =
            date.month.number
                .toString()
                .padStart(2, '0')
        val year = date.year
        "$day.$month.$year"
    } catch (_: Exception) {
        dateString
    }
}
