package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import my.drivebit.components.AppWithHeader
import my.drivebit.components.CarItemSmall
import my.drivebit.components.Column
import my.drivebit.components.DateFieldDialog
import my.drivebit.components.Divider
import my.drivebit.components.FilterChip
import my.drivebit.components.Loader
import my.drivebit.components.PriceFilter
import my.drivebit.components.Row
import my.drivebit.components.Spacer
import my.drivebit.components.TextError
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.viewmodels.DateFieldViewModel
import my.drivebit.viewmodels.SearchPageDateEndViewModel
import my.drivebit.repositories.CurrentFiltersRepository
import my.drivebit.viewmodels.SearchPageDateViewModel
import my.drivebit.viewmodels.SearchState
import my.drivebit.viewmodels.SearchViewModel
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.paddingLeft
import org.jetbrains.compose.web.css.paddingTop
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject
import org.w3c.dom.url.URLSearchParams

@Composable
fun SearchPage() {
    val viewModel: SearchViewModel = koinInject()
    val state by viewModel.state.collectAsState()
    val searchParams = rememberSearchParams()
    val currentFiltersRepository: CurrentFiltersRepository = koinInject()
    val dailyRateMin by currentFiltersRepository.dailyRateMin.collectAsState(null)
    val dailyRateMax by currentFiltersRepository.dailyRateMax.collectAsState(null)
    var showPriceFilter by remember { mutableStateOf(false) }
    val minPrice = remember { mutableStateOf(dailyRateMin?.toInt() ?: 0) }
    val maxPrice = remember { mutableStateOf(dailyRateMax?.toInt() ?: 600) }

    LaunchedEffect(dailyRateMin) {
        dailyRateMin?.let { minPrice.value = it.toInt() }
    }

    LaunchedEffect(dailyRateMax) {
        dailyRateMax?.let { maxPrice.value = it.toInt() }
    }

    val isPriceSelected = dailyRateMin != null || dailyRateMax != null
    val priceText = if (isPriceSelected) {
        "${dailyRateMin?.toInt() ?: 0} - ${dailyRateMax?.toInt() ?: 600}"
    } else {
        null
    }

    AppWithHeader {
        Div({
            style {
                width(100.percent)
                padding(20.px)
                property("max-width", "1200px")
                property("margin", "0 auto")
            }
        }) {
            when (val currentState = state) {
                is SearchState.Loading -> {
                    Loader()
                }

                is SearchState.Error -> {
                    TextError(currentState.message)
                }

                is SearchState.Searching -> {
                    Loader()
                }

                is SearchState.SearchResults -> {
                    Column(gap = 24.px) {
                        Row(gap = 12.px) {
                            SearchPageDateStart(
                                startDate = searchParams.first,
                            )
                            SearchPageDateEnd(
                                endDate = searchParams.second,
                            )
                        }
                        Row {
                            FilterChip(
                                name = "Цена",
                                onClick = { showPriceFilter = true },
                                isSelected = isPriceSelected,
                                selectedText = priceText,
                            )
                        }
                        if (showPriceFilter) {
                            Div({
                                style {
                                    display(DisplayStyle.Flex)
                                    property("justify-content", "flex-start")
                                    width(100.percent)
                                }
                            }) {
                                PriceFilter(
                                    minPrice = minPrice,
                                    maxPrice = maxPrice,
                                    resultsCount = currentState.cars.size,
                                    onReset = {
                                        minPrice.value = 0
                                        maxPrice.value = 600
                                        viewModel.updateDailyRateMin(null)
                                        viewModel.updateDailyRateMax(null)
                                        showPriceFilter = false
                                    },
                                    onViewResults = {
                                        viewModel.updateDailyRateMin(minPrice.value.toDouble())
                                        viewModel.updateDailyRateMax(maxPrice.value.toDouble())
                                        showPriceFilter = false
                                    },
                                )
                            }
                        }
                        if (currentState.cars.isNotEmpty()) {
                            Div({
                                style {
                                    display(DisplayStyle.Grid)
                                    property("grid-template-columns", "repeat(auto-fill, minmax(280px, 1fr))")
                                    gap(24.px)
                                    width(100.percent)
                                }
                            }) {
                                currentState.cars.forEach { car ->
                                    Column {
                                        CarItemSmall(
                                            car = car,
                                            onClick = {
                                                window.location.href = "/car-detail?id=${car.id}"
                                            },
                                        )
                                    }
                                }
                            }
                        } else {
                            Div({
                                style {
                                    padding(40.px)
                                    textAlign("center")
                                }
                            }) {
                                Span({
                                    style {
                                        applyTypography(CSSTypography.Styles.body)
                                        fontSize(CSSTypography.FontSize.base)
                                        color(CSSColors.Gray600)
                                    }
                                }) {
                                    Text("Автомобили не найдены")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberSearchParams(): Pair<String?, String?> {
    val search = window.location.search
    return remember(search) {
        val params = URLSearchParams(search)
        val startDate = params.get("startDate")
        val endDate = params.get("endDate")
        startDate to endDate
    }
}

@Composable
private fun SearchPageDateStart(startDate: String?) {
    val searchPageDateViewModel: SearchPageDateViewModel = koinInject()
    SearchPageDateField(
        label = "Дата начала",
        actionLabel = "c",
        initialDate = startDate,
        onDateChanged = { date -> date?.let(searchPageDateViewModel::set) },
    )
}

@Composable
private fun SearchPageDateEnd(endDate: String?) {
    val searchPageDateEndViewModel: SearchPageDateEndViewModel = koinInject()
    SearchPageDateField(
        label = "Дата окончания",
        actionLabel = "по",
        initialDate = endDate,
        onDateChanged = { date -> date?.let(searchPageDateEndViewModel::set) },
    )
}

@Composable
private fun SearchPageDateField(
    label: String,
    actionLabel: String,
    initialDate: String?,
    onDateChanged: (String?) -> Unit,
) {
    val dateFieldViewModel =
        remember {
            DateFieldViewModel(initialDate = initialDate)
        }
    LaunchedEffect(initialDate) {
        dateFieldViewModel.setDate(initialDate)
    }

    val state by dateFieldViewModel.state.collectAsState()
    val currentDate = state.date ?: "выберите даты"

    Div({
        style {
            cursor("pointer")
            marginBottom(12.px)
        }
        onClick {
            dateFieldViewModel.openCalendar()
        }
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
                    }) {
                        Text(actionLabel)
                    }
                    Span({
                        style {
                            applyTypography(CSSTypography.Styles.body)
                            fontSize(CSSTypography.FontSize.base)
                            fontWeight(CSSTypography.FontWeight.medium)
                            color(CSSColors.Gray600)
                            paddingLeft(12.px)
                        }
                    }) {
                        Text(currentDate)
                    }

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

    DateFieldDialog(
        label = label,
        viewModel = dateFieldViewModel,
        onDateChanged = onDateChanged,
    )
}
