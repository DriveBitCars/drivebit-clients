package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSColors.Blue
import my.drivebit.design.CSSColors.BlueString
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.resources.ImagePaths
import my.drivebit.viewmodels.DateFieldViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Composable
fun DateRangeSelector(
    startDateViewModel: DateFieldViewModel,
    endDateViewModel: DateFieldViewModel,
    onSearchClick: () -> Unit = {},
    startLabel: String = "c",
    endLabel: String = "по",
    showSearchButton: Boolean = true,
) {
    val startState by startDateViewModel.state.collectAsState()
    val endState by endDateViewModel.state.collectAsState()
    val startDate = startState.date
    val endDate = endState.date

    LaunchedEffect(startDate) {
        val currentStartDate = startDate
        val currentEndDate = endDate
        if (currentStartDate != null && currentEndDate != null) {
            if (currentStartDate > currentEndDate) {
                endDateViewModel.setDate(null)
            }
        }
    }

    LaunchedEffect(endDate) {
        val currentStartDate = startDate
        val currentEndDate = endDate
        if (currentStartDate != null && currentEndDate != null) {
            if (currentEndDate < currentStartDate) {
                startDateViewModel.setDate(null)
            }
        }
    }

    Div({
        style {
            width(100.percent)
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            gap(12.px)
            backgroundColor(CSSColors.White)
            borderRadius(12.px)
            padding(4.px, 4.px)
            border(1.px, LineStyle.Solid, CSSColors.Gray300)
            property("transition", "border-color 0.2s ease")
        }
        onMouseEnter {
            (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                "border-color",
                CSSColors.BlueString,
            )
        }
        onMouseLeave {
            (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                "border-color",
                CSSColors.Gray300String,
            )
        }
    }) {
        Div({
            style {
                flex(1)
                marginLeft(12.px)
                cursor("pointer")
            }
            onClick {
                startDateViewModel.openCalendar()
            }
        }) {
            DateField(
                label = startLabel,
                viewModel = startDateViewModel,
            )
        }

        Div({
            style {
                width(1.px)
                height(40.px)
                backgroundColor(CSSColors.Gray300)
                marginLeft(12.px)
                marginRight(12.px)
            }
        })

        Div({
            style {
                flex(1)
                cursor("pointer")
            }
            onClick {
                endDateViewModel.openCalendar()
            }
        }) {
            DateField(
                label = endLabel,
                viewModel = endDateViewModel,
                minDate = startDate,
            )
        }

        if (showSearchButton) {
            Div({
                style {
                    flexShrink(0)
                    marginLeft(12.px)
                    width(48.px)
                }
            }) {
                val navigationController = LocalNavigationController.current
                ActionButton(
                    image = ImagePaths.SEARCH_SVG,
                    enabledColor = Blue,
                    text = "",
                    onClick = {
                        val queryParams =
                            buildString {
                                val currentStartDate = startState.date
                                val currentEndDate = endState.date
                                if (currentStartDate != null) {
                                    append("startDate=$currentStartDate")
                                }
                                if (currentEndDate != null) {
                                    if (length > 0) append("&")
                                    append("endDate=$currentEndDate")
                                }
                            }
                        val citySlug =
                            my.drivebit.utils.parseCitySlugFromSearchPath(kotlinx.browser.window.location.pathname)
                                ?: my.drivebit.web.parseCitySlugFromPath(kotlinx.browser.window.location.pathname)
                                ?: "moskva"
                        val url =
                            my.drivebit.utils.buildCitySearchPath(
                                citySlug,
                                queryParams.takeIf { it.isNotEmpty() },
                            )
                        navigationController?.navigateTo(url)
                        onSearchClick()
                    },
                )
            }
        }
    }

    if (startState.isCalendarOpen || endState.isCalendarOpen) {
        DateRangeCalendarDialog(
            startDateViewModel = startDateViewModel,
            endDateViewModel = endDateViewModel,
        )
    }
}

@Composable
private fun CalendarIconBlue(size: CSSSizeValue<out CSSUnit.px>) {
    Div({
        style {
            width(size)
            height(size)
            backgroundColor(Blue)
            property("mask-image", "url(${ImagePaths.FILTER_MAIN_CALENDAR_SVG})")
            property("mask-size", "contain")
            property("mask-repeat", "no-repeat")
            property("mask-position", "center")
            property("-webkit-mask-image", "url(${ImagePaths.FILTER_MAIN_CALENDAR_SVG})")
            property("-webkit-mask-size", "contain")
            property("-webkit-mask-repeat", "no-repeat")
            property("-webkit-mask-position", "center")
        }
    })
}

@Composable
fun HeroDateRangeSelector(
    startDateViewModel: DateFieldViewModel,
    endDateViewModel: DateFieldViewModel,
    compact: Boolean = false,
    onRangeConfirmed: (() -> Unit)? = null,
) {
    val startState by startDateViewModel.state.collectAsState()
    val endState by endDateViewModel.state.collectAsState()
    val startDate = startState.date
    val endDate = endState.date

    LaunchedEffect(startDate) {
        val currentStartDate = startDate
        val currentEndDate = endDate
        if (currentStartDate != null && currentEndDate != null && currentStartDate > currentEndDate) {
            endDateViewModel.setDate(null)
        }
    }

    LaunchedEffect(endDate) {
        val currentStartDate = startDate
        val currentEndDate = endDate
        if (currentStartDate != null && currentEndDate != null && currentEndDate < currentStartDate) {
            startDateViewModel.setDate(null)
        }
    }

    Div({
        style {
            width(100.percent)
            height(if (compact) 72.px else 90.px)
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            gap(0.px)
            backgroundColor(CSSColors.White)
            borderRadius(20.px)
            padding(0.px, if (compact) 16.px else 34.px)
            property("box-sizing", "border-box")
        }
        onMouseEnter {
            (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                "border",
                "none",
            )
        }
    }) {
        Div({
            style {
                flex(1)
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.SpaceBetween)
                gap(if (compact) 8.px else 12.px)
                cursor("pointer")
            }
            onClick { startDateViewModel.openCalendar() }
        }) {
            DateField(
                label = if (compact) "с" else "Начало аренды",
                viewModel = startDateViewModel,
            )
            CalendarIconBlue(size = if (compact) 20.px else 24.px)
        }

        Div({
            style {
                width(1.px)
                height(if (compact) 48.px else 64.px)
                backgroundColor(CSSColors.Gray300)
                marginLeft(if (compact) 12.px else 24.px)
                marginRight(if (compact) 12.px else 24.px)
            }
        })

        Div({
            style {
                flex(1)
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.SpaceBetween)
                gap(if (compact) 8.px else 12.px)
                cursor("pointer")
            }
            onClick { endDateViewModel.openCalendar() }
        }) {
            DateField(
                label = if (compact) "по" else "Завершение аренды",
                viewModel = endDateViewModel,
                minDate = startDate,
            )
            CalendarIconBlue(size = if (compact) 20.px else 24.px)
        }
    }

    if (startState.isCalendarOpen || endState.isCalendarOpen) {
        DateRangeCalendarDialog(
            startDateViewModel = startDateViewModel,
            endDateViewModel = endDateViewModel,
            onConfirm = onRangeConfirmed,
        )
    }
}
