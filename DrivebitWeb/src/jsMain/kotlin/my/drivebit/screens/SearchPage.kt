package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import my.drivebit.shell.AppWithHeader
import my.drivebit.components.BodyTypeFilter
import my.drivebit.components.Box
import my.drivebit.components.BoxOverlay
import my.drivebit.components.BrandModelFilter
import my.drivebit.components.CarsGrid
import my.drivebit.components.Column
import my.drivebit.components.DriveTypeFilter
import my.drivebit.components.FilterChip
import my.drivebit.components.FiltersResetChip
import my.drivebit.components.FlowRow
import my.drivebit.components.Loader
import my.drivebit.components.MileageFilter
import my.drivebit.components.mileageFilterLabelForMin
import my.drivebit.components.PaginationBar
import my.drivebit.components.PriceFilter
import my.drivebit.components.SearchDateRangeSelector
import my.drivebit.components.SeatsFilter
import my.drivebit.components.YEAR_FILTER_MIN
import my.drivebit.components.YearFilter
import my.drivebit.components.currentCalendarYear
import my.drivebit.components.TextError
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.navigation.NavigationState
import my.drivebit.repositories.CurrentFiltersRepository
import my.drivebit.web.BrandSlugResolver
import my.drivebit.web.buildCarDetailUrl
import my.drivebit.web.parseSearchBrandSlugFromPath
import my.drivebit.web.searchPathForBrandName
import my.drivebit.viewmodels.SearchPageDateEndViewModel
import my.drivebit.viewmodels.SearchPageDateViewModel
import my.drivebit.viewmodels.SearchState
import my.drivebit.viewmodels.SearchViewModel
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import org.w3c.dom.url.URLSearchParams

@Composable
fun SearchPage() {
    val navigationController = LocalNavigationController.current!!
    val navigationState: NavigationState = koinInject()
    val brandSlugResolver: BrandSlugResolver = koinInject()
    val viewModel: SearchViewModel = koinInject()
    val state by viewModel.state.collectAsState()
    val displayedCars by viewModel.displayedCars.collectAsState()
    val paginationInfo by viewModel.paginationInfo.collectAsState()
    var locationSearch by remember { mutableStateOf(window.location.search) }
    val searchParams = rememberSearchParams(locationSearch)
    val currentFiltersRepository: CurrentFiltersRepository = koinInject(named("search"))
    val dailyRateMin by currentFiltersRepository.dailyRateMin.collectAsState(null)
    val dailyRateMax by currentFiltersRepository.dailyRateMax.collectAsState(null)
    val filterBrandName by currentFiltersRepository.brandName.collectAsState(null)
    val filterModelName by currentFiltersRepository.modelName.collectAsState(null)
    val filterDriveTypeTranslate by currentFiltersRepository.driveTypeTranslate.collectAsState(null)
    val filterBodyTypeTranslate by currentFiltersRepository.bodyTypeTranslate.collectAsState(null)
    val filterSeatsMin by currentFiltersRepository.seatsMin.collectAsState(null)
    val filterYearMin by currentFiltersRepository.yearMin.collectAsState(null)
    val filterYearMax by currentFiltersRepository.yearMax.collectAsState(null)
    val filterMileageMin by currentFiltersRepository.availableMileagePerDayKmMin.collectAsState(null)
    val startDateByRepo by currentFiltersRepository.startState.collectAsState(null)
    val endDateByRepo by currentFiltersRepository.endState.collectAsState(null)
    val currentPath by navigationState.currentPath.collectAsState()
    val brandSlugFromPath = parseSearchBrandSlugFromPath(currentPath)

    LaunchedEffect(brandSlugFromPath) {
        val slug = brandSlugFromPath ?: return@LaunchedEffect
        val brand = brandSlugResolver.resolve(slug) ?: return@LaunchedEffect
        if (filterBrandName != brand.name) {
            viewModel.updateBrand(brand.id, brand.name)
            viewModel.updateModel(null, null)
        }
    }

    LaunchedEffect(searchParams.first, searchParams.second) {
        searchParams.first?.let { currentFiltersRepository.updateStartDate(it) }
        searchParams.second?.let { currentFiltersRepository.updateEndDate(it) }
    }

    var showPriceFilter by remember { mutableStateOf(false) }
    var showBrandFilter by remember { mutableStateOf(false) }
    var showDriveTypeFilter by remember { mutableStateOf(false) }
    var showBodyTypeFilter by remember { mutableStateOf(false) }
    var showSeatsFilter by remember { mutableStateOf(false) }
    var showYearFilter by remember { mutableStateOf(false) }
    var showMileageFilter by remember { mutableStateOf(false) }
    val minPrice = remember { mutableStateOf(dailyRateMin ?: 0) }
    val maxPrice = remember { mutableStateOf(dailyRateMax ?: 50000) }
    val minYear = remember { mutableStateOf(filterYearMin ?: YEAR_FILTER_MIN) }
    val maxYear = remember { mutableStateOf(filterYearMax ?: currentCalendarYear()) }

    LaunchedEffect(dailyRateMin) {
        dailyRateMin?.let { minPrice.value = it }
    }

    LaunchedEffect(dailyRateMax) {
        dailyRateMax?.let { maxPrice.value = it }
    }

    LaunchedEffect(filterYearMin) {
        filterYearMin?.let { minYear.value = it }
    }

    LaunchedEffect(filterYearMax) {
        filterYearMax?.let { maxYear.value = it }
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
    val isYearSelected = filterYearMin != null || filterYearMax != null
    val yearChipText =
        if (isYearSelected) {
            "${filterYearMin ?: YEAR_FILTER_MIN} — ${filterYearMax ?: currentCalendarYear()}"
        } else {
            null
        }
    val isMileageSelected = filterMileageMin != null
    val mileageChipText = mileageFilterLabelForMin(filterMileageMin)
    val hasDatesSelected =
        startDateByRepo != null ||
            endDateByRepo != null ||
            searchParams.first != null ||
            searchParams.second != null
    val hasAnyFilter =
        isPriceSelected ||
            isBrandSelected ||
            isDriveTypeSelected ||
            isBodyTypeSelected ||
            isSeatsSelected ||
            isYearSelected ||
            isMileageSelected ||
            hasDatesSelected

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
                    val searchPageDateViewModel: SearchPageDateViewModel = koinInject()
                    val searchPageDateEndViewModel: SearchPageDateEndViewModel = koinInject()
                    Column(gap = 24.px) {
                        SearchDateRangeSelector(
                            startDate = startDateByRepo ?: searchParams.first,
                            endDate = endDateByRepo ?: searchParams.second,
                            onStartDateChanged = { date -> searchPageDateViewModel.set(date) },
                            onEndDateChanged = { date -> searchPageDateEndViewModel.set(date) },
                        )
                        FlowRow(gap = 8.px) {
                            FilterChip(
                                name = "Марка",
                                onClick = {
                                    showBrandFilter = !showBrandFilter
                                    showPriceFilter = false
                                    showDriveTypeFilter = false
                                    showBodyTypeFilter = false
                                    showSeatsFilter = false
                                    showYearFilter = false
                                    showMileageFilter = false
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
                                    showYearFilter = false
                                    showMileageFilter = false
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
                                    showYearFilter = false
                                    showMileageFilter = false
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
                                    showYearFilter = false
                                    showMileageFilter = false
                                },
                                isSelected = isSeatsSelected,
                                selectedText = seatsChipText,
                            )
                            FilterChip(
                                name = "Год выпуска",
                                onClick = {
                                    showYearFilter = !showYearFilter
                                    showPriceFilter = false
                                    showBrandFilter = false
                                    showDriveTypeFilter = false
                                    showBodyTypeFilter = false
                                    showSeatsFilter = false
                                    showMileageFilter = false
                                },
                                isSelected = isYearSelected,
                                selectedText = yearChipText,
                            )
                            FilterChip(
                                name = "Километраж",
                                onClick = {
                                    showMileageFilter = !showMileageFilter
                                    showPriceFilter = false
                                    showBrandFilter = false
                                    showDriveTypeFilter = false
                                    showBodyTypeFilter = false
                                    showSeatsFilter = false
                                    showYearFilter = false
                                },
                                isSelected = isMileageSelected,
                                selectedText = mileageChipText,
                            )
                            FilterChip(
                                name = "Цена",
                                onClick = {
                                    showPriceFilter = !showPriceFilter
                                    showBrandFilter = false
                                    showDriveTypeFilter = false
                                    showBodyTypeFilter = false
                                    showSeatsFilter = false
                                    showYearFilter = false
                                    showMileageFilter = false
                                },
                                isSelected = isPriceSelected,
                                selectedText = priceText,
                            )
                            if (hasAnyFilter) {
                                FiltersResetChip(
                                    onClick = {
                                        showPriceFilter = false
                                        showBrandFilter = false
                                        showDriveTypeFilter = false
                                        showBodyTypeFilter = false
                                        showSeatsFilter = false
                                        showYearFilter = false
                                        showMileageFilter = false
                                        minPrice.value = 0
                                        maxPrice.value = 50000
                                        minYear.value = YEAR_FILTER_MIN
                                        maxYear.value = currentCalendarYear()
                                        viewModel.resetAllFilters()
                                        searchPageDateViewModel.set(null)
                                        searchPageDateEndViewModel.set(null)
                                        val targetPath =
                                            if (brandSlugFromPath != null) "/search" else window.location.pathname
                                        window.history.pushState(null, "", targetPath)
                                        locationSearch = ""
                                        if (brandSlugFromPath != null) {
                                            navigationState.updatePath("/search")
                                        }
                                    },
                                )
                            }
                        }
                        Box(
                            modifier = {
                                property("min-height", "240px")
                            },
                            underneath = {
                                if (currentState.cars.isNotEmpty()) {
                                    CarsGrid(
                                        cars = displayedCars,
                                        carHref = { car ->
                                            buildCarDetailUrl(
                                                carId = car.id,
                                                startDate = startDateByRepo ?: searchParams.first,
                                                endDate = endDateByRepo ?: searchParams.second,
                                            )
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
                            },
                            overlay = {
                                if (showPriceFilter) {
                                    BoxOverlay {
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
                                    BoxOverlay {
                                        BrandModelFilter(
                                            onBrandSelected = { brandId, brandName ->
                                                viewModel.updateBrand(brandId, brandName)
                                                viewModel.updateModel(null, null)
                                                val targetPath = searchPathForBrandName(brandName)
                                                if (currentPath != targetPath) {
                                                    val query = window.location.search
                                                    window.history.pushState(null, "", "$targetPath$query")
                                                    navigationState.updatePath(targetPath)
                                                }
                                            },
                                            onModelSelected = { modelId, modelName ->
                                                viewModel.updateModel(modelId, modelName)
                                                showBrandFilter = false
                                            },
                                            onReset = {
                                                viewModel.updateBrand(null, null)
                                                viewModel.updateModel(null, null)
                                                showBrandFilter = false
                                                if (brandSlugFromPath != null) {
                                                    val query = window.location.search
                                                    window.history.pushState(null, "", "/search$query")
                                                    navigationState.updatePath("/search")
                                                }
                                            },
                                            onOk = { showBrandFilter = false },
                                        )
                                    }
                                }
                                if (showDriveTypeFilter) {
                                    BoxOverlay {
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
                                    BoxOverlay {
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
                                    BoxOverlay {
                                        SeatsFilter(
                                            selectedSeatsMin = filterSeatsMin,
                                            resultsCount = currentState.totalCount,
                                            onSeatsMinSelected = { seatsMin ->
                                                viewModel.updateSeatsMin(seatsMin)
                                                showSeatsFilter = false
                                            },
                                            onReset = {
                                                viewModel.updateSeatsMin(null)
                                                showSeatsFilter = false
                                            },
                                        )
                                    }
                                }
                                if (showYearFilter) {
                                    BoxOverlay {
                                        YearFilter(
                                            minYear = minYear,
                                            maxYear = maxYear,
                                            onReset = {
                                                minYear.value = YEAR_FILTER_MIN
                                                maxYear.value = currentCalendarYear()
                                                viewModel.updateYearMin(null)
                                                viewModel.updateYearMax(null)
                                                showYearFilter = false
                                            },
                                            onViewResults = {
                                                viewModel.updateYearMin(minYear.value)
                                                viewModel.updateYearMax(maxYear.value)
                                                showYearFilter = false
                                            },
                                        )
                                    }
                                }
                                if (showMileageFilter) {
                                    BoxOverlay {
                                        MileageFilter(
                                            selectedMileageMin = filterMileageMin,
                                            onMileageMinSelected = { mileageMin ->
                                                viewModel.updateAvailableMileagePerDayKmMin(mileageMin)
                                                showMileageFilter = false
                                            },
                                            onReset = {
                                                viewModel.updateAvailableMileagePerDayKmMin(null)
                                                showMileageFilter = false
                                            },
                                        )
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberSearchParams(locationSearch: String): Pair<String?, String?> {
    return remember(locationSearch) {
        val params = URLSearchParams(locationSearch)
        val startDate = params.get("startDate")
        val endDate = params.get("endDate")
        startDate to endDate
    }
}
