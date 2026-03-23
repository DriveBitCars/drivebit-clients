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
import my.drivebit.components.BodyTypeFilter
import my.drivebit.components.BrandModelFilter
import my.drivebit.components.CarsGrid
import my.drivebit.components.Column
import my.drivebit.components.DateFieldDialog
import my.drivebit.components.Divider
import my.drivebit.components.DriveTypeFilter
import my.drivebit.components.FilterChip
import my.drivebit.components.Loader
import my.drivebit.components.PaginationBar
import my.drivebit.components.PriceFilter
import my.drivebit.components.Row
import my.drivebit.components.SeatsFilter
import my.drivebit.components.TextError
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.repositories.CurrentFiltersRepository
import my.drivebit.utils.END_AT
import my.drivebit.utils.START_AT
import my.drivebit.utils.dateToEndAtIso
import my.drivebit.utils.dateToStartAtIso
import my.drivebit.utils.encodeUrlParameter
import my.drivebit.viewmodels.DateFieldViewModel
import my.drivebit.viewmodels.SearchPageDateEndViewModel
import my.drivebit.viewmodels.SearchPageDateViewModel
import my.drivebit.viewmodels.SearchState
import my.drivebit.viewmodels.SearchViewModel
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
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
import org.koin.core.qualifier.named
import org.w3c.dom.url.URLSearchParams

@Composable
fun SearchPage() {
    val viewModel: SearchViewModel = koinInject()
    val state by viewModel.state.collectAsState()
    val displayedCars by viewModel.displayedCars.collectAsState()
    val paginationInfo by viewModel.paginationInfo.collectAsState()
    val searchParams = rememberSearchParams()
    val currentFiltersRepository: CurrentFiltersRepository = koinInject(named("search"))
    val dailyRateMin by currentFiltersRepository.dailyRateMin.collectAsState(null)
    val dailyRateMax by currentFiltersRepository.dailyRateMax.collectAsState(null)
    val filterBrandName by currentFiltersRepository.brandName.collectAsState(null)
    val filterModelName by currentFiltersRepository.modelName.collectAsState(null)
    val filterDriveTypeTranslate by currentFiltersRepository.driveTypeTranslate.collectAsState(null)
    val filterBodyTypeTranslate by currentFiltersRepository.bodyTypeTranslate.collectAsState(null)
    val filterSeatsMin by currentFiltersRepository.seatsMin.collectAsState(null)
    val startDateByRepo by currentFiltersRepository.startState.collectAsState(null)
    val endDateByRepo by currentFiltersRepository.endState.collectAsState(null)

    LaunchedEffect(searchParams.first, searchParams.second) {
        searchParams.first?.let { currentFiltersRepository.updateStartDate(it) }
        searchParams.second?.let { currentFiltersRepository.updateEndDate(it) }
    }

    var showPriceFilter by remember { mutableStateOf(false) }
    var showBrandFilter by remember { mutableStateOf(false) }
    var showDriveTypeFilter by remember { mutableStateOf(false) }
    var showBodyTypeFilter by remember { mutableStateOf(false) }
    var showSeatsFilter by remember { mutableStateOf(false) }
    val minPrice = remember { mutableStateOf(dailyRateMin ?: 0) }
    val maxPrice = remember { mutableStateOf(dailyRateMax ?: 50000) }

    LaunchedEffect(dailyRateMin) {
        dailyRateMin?.let { minPrice.value = it }
    }

    LaunchedEffect(dailyRateMax) {
        dailyRateMax?.let { maxPrice.value = it }
    }

    val isPriceSelected = dailyRateMin != null || dailyRateMax != null
    val priceText =
        if (isPriceSelected) {
            "${dailyRateMin ?: 0} - ${dailyRateMax ?: 50000}"
        } else {
            null
        }

    val isBrandSelected = filterBrandName != null
    val brandChipText =
        when {
            filterBrandName != null && filterModelName != null -> "$filterBrandName $filterModelName"
            filterBrandName != null -> filterBrandName
            else -> null
        }

    val isDriveTypeSelected = filterDriveTypeTranslate != null
    val isBodyTypeSelected = filterBodyTypeTranslate != null
    val isSeatsSelected = filterSeatsMin != null
    val seatsChipText = filterSeatsMin?.let { "$it или более" }

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
                        Row(gap = 8.px) {
                            FilterChip(
                                name = "Цена",
                                onClick = {
                                    showPriceFilter = !showPriceFilter
                                    showBrandFilter = false
                                    showDriveTypeFilter = false
                                    showBodyTypeFilter = false
                                    showSeatsFilter = false
                                },
                                isSelected = isPriceSelected,
                                selectedText = priceText,
                            )
                            FilterChip(
                                name = "Марка",
                                onClick = {
                                    showBrandFilter = !showBrandFilter
                                    showPriceFilter = false
                                    showDriveTypeFilter = false
                                    showBodyTypeFilter = false
                                    showSeatsFilter = false
                                },
                                isSelected = isBrandSelected,
                                selectedText = brandChipText,
                            )
                            FilterChip(
                                name = "Привод",
                                onClick = {
                                    showDriveTypeFilter = !showDriveTypeFilter
                                    showPriceFilter = false
                                    showBrandFilter = false
                                    showBodyTypeFilter = false
                                    showSeatsFilter = false
                                },
                                isSelected = isDriveTypeSelected,
                                selectedText = filterDriveTypeTranslate,
                            )
                            FilterChip(
                                name = "Кузов",
                                onClick = {
                                    showBodyTypeFilter = !showBodyTypeFilter
                                    showPriceFilter = false
                                    showBrandFilter = false
                                    showDriveTypeFilter = false
                                    showSeatsFilter = false
                                },
                                isSelected = isBodyTypeSelected,
                                selectedText = filterBodyTypeTranslate,
                            )
                            FilterChip(
                                name = "Количество мест",
                                onClick = {
                                    showSeatsFilter = !showSeatsFilter
                                    showPriceFilter = false
                                    showBrandFilter = false
                                    showDriveTypeFilter = false
                                    showBodyTypeFilter = false
                                },
                                isSelected = isSeatsSelected,
                                selectedText = seatsChipText,
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
                                    resultsCount = currentState.totalCount,
                                    onReset = {
                                        minPrice.value = 0
                                        maxPrice.value = 50000
                                        viewModel.updateDailyRateMin(null)
                                        viewModel.updateDailyRateMax(null)
                                        showPriceFilter = false
                                    },
                                    onViewResults = {
                                        viewModel.updateDailyRateMin(minPrice.value)
                                        viewModel.updateDailyRateMax(maxPrice.value)
                                        showPriceFilter = false
                                    },
                                )
                            }
                        }
                        if (showBrandFilter) {
                            Div({
                                style {
                                    display(DisplayStyle.Flex)
                                    property("justify-content", "flex-start")
                                    width(100.percent)
                                }
                            }) {
                                BrandModelFilter(
                                    onBrandSelected = { brandId, brandName ->
                                        viewModel.updateBrand(brandId, brandName)
                                        viewModel.updateModel(null, null)
                                    },
                                    onModelSelected = { modelId, modelName ->
                                        viewModel.updateModel(modelId, modelName)
                                        showBrandFilter = false
                                    },
                                    onReset = {
                                        viewModel.updateBrand(null, null)
                                        viewModel.updateModel(null, null)
                                        showBrandFilter = false
                                    },
                                )
                            }
                        }
                        if (showDriveTypeFilter) {
                            Div({
                                style {
                                    display(DisplayStyle.Flex)
                                    property("justify-content", "flex-start")
                                    width(100.percent)
                                }
                            }) {
                                DriveTypeFilter(
                                    onDriveTypeSelected = { name, translate ->
                                        viewModel.updateDriveType(name, translate)
                                        showDriveTypeFilter = false
                                    },
                                    onReset = {
                                        viewModel.updateDriveType(null, null)
                                        showDriveTypeFilter = false
                                    },
                                )
                            }
                        }
                        if (showBodyTypeFilter) {
                            Div({
                                style {
                                    display(DisplayStyle.Flex)
                                    property("justify-content", "flex-start")
                                    width(100.percent)
                                }
                            }) {
                                BodyTypeFilter(
                                    onBodyTypeSelected = { name, translate ->
                                        viewModel.updateBodyType(name, translate)
                                        showBodyTypeFilter = false
                                    },
                                    onReset = {
                                        viewModel.updateBodyType(null, null)
                                        showBodyTypeFilter = false
                                    },
                                )
                            }
                        }
                        if (showSeatsFilter) {
                            Div({
                                style {
                                    display(DisplayStyle.Flex)
                                    property("justify-content", "flex-start")
                                    width(100.percent)
                                }
                            }) {
                                SeatsFilter(
                                    selectedSeatsMin = filterSeatsMin,
                                    resultsCount = currentState.totalCount,
                                    onSeatsMinSelected = { seatsMin ->
                                        viewModel.updateSeatsMin(seatsMin)
                                    },
                                    onReset = {
                                        viewModel.updateSeatsMin(null)
                                        showSeatsFilter = false
                                    },
                                )
                            }
                        }
                        if (currentState.cars.isNotEmpty()) {
                            CarsGrid(
                                cars = displayedCars,
                                onCarClick = { car ->
                                    val startDate = startDateByRepo ?: searchParams.first
                                    val endDate = endDateByRepo ?: searchParams.second
                                    val params = mutableListOf("id=${car.id.encodeUrlParameter()}")
                                    dateToStartAtIso(startDate)?.let {
                                        params.add("$START_AT=${it.encodeUrlParameter()}")
                                    }
                                    dateToEndAtIso(endDate)?.let {
                                        params.add("$END_AT=${it.encodeUrlParameter()}")
                                    }
                                    window.location.href = "/car-detail?${params.joinToString("&")}"
                                },
                            )
                            PaginationBar(
                                currentPage = paginationInfo.first,
                                totalPages = paginationInfo.second,
                                totalCount = paginationInfo.third,
                                pageSize = 9,
                                onPageChange = { viewModel.setPage(it) },
                            )
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
