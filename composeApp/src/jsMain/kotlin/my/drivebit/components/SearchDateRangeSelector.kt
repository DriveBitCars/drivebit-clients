package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.viewmodels.DateFieldViewModel
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.paddingLeft
import org.jetbrains.compose.web.css.paddingTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun SearchDateRangeSelector(
    startDate: String?,
    endDate: String?,
    onStartDateChanged: (String?) -> Unit,
    onEndDateChanged: (String?) -> Unit,
    disabledDates: Set<String> = emptySet(),
    endMinOffsetDaysFromStart: Int = 0,
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

    LaunchedEffect(startState.date) { onStartDateChanged(startState.date) }
    LaunchedEffect(endState.date) { onEndDateChanged(endState.date) }

    Row(gap = 12.px) {
        SearchDateFieldItem(
            actionLabel = "c",
            value = startState.date ?: "выберите даты",
            onClick = { startViewModel.openCalendar() },
        )
        SearchDateFieldItem(
            actionLabel = "по",
            value = endState.date ?: "выберите даты",
            onClick = { endViewModel.openCalendar() },
        )
    }

    if (startState.isCalendarOpen || endState.isCalendarOpen) {
        DateRangeCalendarDialog(
            startDateViewModel = startViewModel,
            endDateViewModel = endViewModel,
            disabledDates = disabledDates,
            endMinOffsetDaysFromStart = endMinOffsetDaysFromStart,
        )
    }
}

@Composable
private fun SearchDateFieldItem(
    actionLabel: String,
    value: String,
    onClick: () -> Unit,
) {
    Div({
        style {
            cursor("pointer")
            marginBottom(12.px)
        }
        onClick { onClick() }
    }) {
        Row(
            alignItems = AlignItems.Center,
            gap = 6.px,
        ) {
            Column(gap = 4.px) {
                Row {
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
                            color(CSSColors.Gray600)
                            paddingLeft(12.px)
                        }
                    }) { Text(value) }
                    Img(
                        src = "/images/arrow-bottom.svg",
                        alt = "arrow",
                        attrs = {
                            style {
                                width(16.px)
                                height(10.px)
                                paddingTop(8.px)
                            }
                        },
                    )
                }
                Divider(
                    color = CSSColors.Gray300,
                    thickness = 2.px,
                )
            }
        }
    }
}
