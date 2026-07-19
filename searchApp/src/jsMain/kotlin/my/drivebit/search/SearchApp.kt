package my.drivebit.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import kotlinx.coroutines.flow.first
import my.drivebit.components.BodyTypeFilter
import my.drivebit.components.Box
import my.drivebit.components.BoxOverlay
import my.drivebit.components.BrandModelFilter
import my.drivebit.components.CarsGrid
import my.drivebit.components.CarsGridSkeleton
import my.drivebit.components.Column
import my.drivebit.components.DriveTypeFilter
import my.drivebit.components.FilterChip
import my.drivebit.components.FiltersResetChip
import my.drivebit.components.FlowRow
import my.drivebit.components.MileageFilter
import my.drivebit.components.PaginationBar
import my.drivebit.components.PriceFilter
import my.drivebit.components.SearchDateRangeSelector
import my.drivebit.components.SeatsFilter
import my.drivebit.components.TextError
import my.drivebit.components.YEAR_FILTER_MIN
import my.drivebit.components.YearFilter
import my.drivebit.components.currentCalendarYear
import my.drivebit.components.mileageFilterLabelForMin
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.utils.cityNameToSlug
import my.drivebit.utils.parseSearchUrl
import my.drivebit.utils.resolveBareSearchRedirectPath
import my.drivebit.utils.uiIndexToUrlPage
import my.drivebit.utils.urlPageToUiIndex
import my.drivebit.repositories.MyCityRepository
import my.drivebit.web.CitySlugResolver
import my.drivebit.web.buildCarDetailUrl
import my.drivebit.web.canonicalSearchBrandPath
import my.drivebit.web.resolveSearchPageHeadline
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject
import org.w3c.dom.events.Event

@Composable
fun SearchApp() {
    val viewModel: SearchViewModel = koinInject()
    val citySlugResolver: CitySlugResolver = koinInject()
    val myCityRepository: MyCityRepository = koinInject()
    var locationHref by remember { mutableStateOf(currentLocationHref()) }
    val state by viewModel.state.collectAsState()
    var searchCityName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val onPopState: (Event) -> Unit = {
            locationHref = currentLocationHref()
        }
        window.addEventListener("popstate", onPopState)
    }

    LaunchedEffect(locationHref) {
        val path =
            locationHref
                .substringBefore('?')
                .substringBefore('#')
                .removeSuffix("/")
                .ifEmpty { "/" }
        if (path == "/search") {
            val cityName = runCatching { myCityRepository.getSelectedCity.first().name }.getOrNull()
            val query = window.location.search.removePrefix("?").takeIf { it.isNotEmpty() }
            val target = resolveBareSearchRedirectPath(cityName, query)
            window.history.replaceState(null, "", target)
            locationHref = currentLocationHref()
            return@LaunchedEffect
        }
        val canonical = canonicalSearchBrandPath(path)
        if (canonical != null && canonical != path) {
            window.history.replaceState(null, "", "$canonical${window.location.search}")
            locationHref = currentLocationHref()
            return@LaunchedEffect
        }
        val urlParts = parseSearchUrl(locationHref)
        val citySlugFromUrl = urlParts.citySlug
        if (citySlugFromUrl != null) {
            val city =
                citySlugResolver.resolve(citySlugFromUrl)
                    ?: citySlugResolver.moscowCity()
            myCityRepository.selectCity(city.id, city.name)
            searchCityName = city.name
        } else {
            searchCityName = myCityRepository.getSelectedCity.first().name
        }
        viewModel.load(locationHref)
    }

    var lastFilters by remember { mutableStateOf<SearchFilterSet?>(null) }

    LaunchedEffect(state) {
        val current = state
        if (current is SearchUiState.Results) {
            lastFilters = current.filters
        }
    }

    val filtersForUi =
        when (val s = state) {
            is SearchUiState.Results -> s.filters
            else -> {
                val fromUrl = filtersFromLocation(locationHref)
                val previous = lastFilters
                if (previous != null &&
                    previous.brandSlug == fromUrl.brandSlug &&
                    previous.modelSlug == fromUrl.modelSlug &&
                    previous.citySlug == fromUrl.citySlug
                ) {
                    fromUrl.copy(
                        brandId = previous.brandId,
                        brandName = previous.brandName,
                        modelId = previous.modelId,
                        modelName = previous.modelName,
                    )
                } else {
                    fromUrl
                }
            }
        }

    val pathOnly = locationHref.substringBefore('?').substringBefore('#')
    val pageHeadline =
        resolveSearchPageHeadline(
            path = pathOnly,
            brandName = filtersForUi.brandName,
            cityName = searchCityName,
        )

    val resultsState = state as? SearchUiState.Results
    val errorMessage = (state as? SearchUiState.Error)?.message

    Div({
        style {
            width(100.percent)
            padding(20.px)
            property("max-width", "1200px")
            property("margin", "0 auto")
            property("box-sizing", "border-box")
        }
    }) {
        SearchResultsContent(
            filters = resultsState?.filters ?: filtersForUi,
            result = resultsState?.result,
            pageHeadline = pageHeadline,
            errorMessage = errorMessage,
            onLocationChanged = { locationHref = currentLocationHref() },
        )
    }
}

private fun filtersFromLocation(locationHref: String): SearchFilterSet {
    val parts = parseSearchUrl(locationHref)
    return SearchFilterSet(
        citySlug = parts.citySlug,
        brandSlug = parts.brandSlug,
        modelSlug = parts.modelSlug,
        startDate = parts.startDate,
        endDate = parts.endDate,
        dailyRateMin = parts.dailyRateMin,
        dailyRateMax = parts.dailyRateMax,
        driveType = parts.driveType,
        driveTypeLabel = parts.driveTypeLabel,
        bodyType = parts.bodyType,
        bodyTypeLabel = parts.bodyTypeLabel,
        seatsMin = parts.seatsMin,
        yearMin = parts.yearMin,
        yearMax = parts.yearMax,
        mileageMin = parts.mileageMin,
        page = parts.page,
    )
}

@Composable
private fun SearchResultsContent(
    filters: SearchFilterSet,
    result: SearchCarsResult?,
    pageHeadline: String,
    errorMessage: String? = null,
    onLocationChanged: () -> Unit,
) {
    var showPriceFilter by remember { mutableStateOf(false) }
    var showBrandFilter by remember { mutableStateOf(false) }
    var showDriveTypeFilter by remember { mutableStateOf(false) }
    var showBodyTypeFilter by remember { mutableStateOf(false) }
    var showSeatsFilter by remember { mutableStateOf(false) }
    var showYearFilter by remember { mutableStateOf(false) }
    var showMileageFilter by remember { mutableStateOf(false) }
    val minPrice = remember { mutableStateOf(filters.dailyRateMin ?: 0) }
    val maxPrice = remember { mutableStateOf(filters.dailyRateMax ?: 50000) }
    val minYear = remember { mutableStateOf(filters.yearMin ?: YEAR_FILTER_MIN) }
    val maxYear = remember { mutableStateOf(filters.yearMax ?: currentCalendarYear()) }

    LaunchedEffect(filters.dailyRateMin) {
        filters.dailyRateMin?.let { minPrice.value = it }
    }
    LaunchedEffect(filters.dailyRateMax) {
        filters.dailyRateMax?.let { maxPrice.value = it }
    }
    LaunchedEffect(filters.yearMin) {
        filters.yearMin?.let { minYear.value = it }
    }
    LaunchedEffect(filters.yearMax) {
        filters.yearMax?.let { maxYear.value = it }
    }

    fun closeOverlays() {
        showPriceFilter = false
        showBrandFilter = false
        showDriveTypeFilter = false
        showBodyTypeFilter = false
        showSeatsFilter = false
        showYearFilter = false
        showMileageFilter = false
    }

    fun navigateFilter(transform: (SearchFilterSet) -> SearchFilterSet) {
        navigateSearchFilters(applySearchFilterChange(filters, transform))
        onLocationChanged()
    }

    fun navigatePage(page: Int) {
        navigateSearchFilters(filters.copy(page = page))
        onLocationChanged()
    }

    val isPriceSelected = filters.dailyRateMin != null || filters.dailyRateMax != null
    val priceText =
        if (isPriceSelected) {
            "${filters.dailyRateMin ?: 0} - ${filters.dailyRateMax ?: 50000}"
        } else {
            null
        }
    val isBrandSelected = filters.brandName != null
    val brandChipText =
        when {
            filters.brandName != null && filters.modelName != null ->
                "${filters.brandName} ${filters.modelName}"
            filters.brandName != null -> filters.brandName
            else -> null
        }
    val isDriveTypeSelected = filters.driveTypeLabel != null
    val isBodyTypeSelected = filters.bodyTypeLabel != null
    val isSeatsSelected = filters.seatsMin != null
    val seatsChipText = filters.seatsMin?.let { "$it или более" }
    val isYearSelected = filters.yearMin != null || filters.yearMax != null
    val yearChipText =
        if (isYearSelected) {
            "${filters.yearMin ?: YEAR_FILTER_MIN} — ${filters.yearMax ?: currentCalendarYear()}"
        } else {
            null
        }
    val isMileageSelected = filters.mileageMin != null
    val mileageChipText = mileageFilterLabelForMin(filters.mileageMin)
    val hasDatesSelected = filters.startDate != null || filters.endDate != null
    val hasAnyFilter =
        isPriceSelected ||
            isBrandSelected ||
            isDriveTypeSelected ||
            isBodyTypeSelected ||
            isSeatsSelected ||
            isYearSelected ||
            isMileageSelected ||
            hasDatesSelected

    Column(gap = 24.px) {
        SearchPageHeadline(pageHeadline)
        SearchDateRangeSelector(
            startDate = filters.startDate,
            endDate = filters.endDate,
            applyOnlyCompleteRange = true,
            onDateRangeChanged = { start, end ->
                navigateFilter { it.copy(startDate = start, endDate = end) }
            },
        )
        if (errorMessage != null) {
            TextError(errorMessage)
        }
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
                selectedText = filters.driveTypeLabel,
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
                selectedText = filters.bodyTypeLabel,
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
                        closeOverlays()
                        minPrice.value = 0
                        maxPrice.value = 50000
                        minYear.value = YEAR_FILTER_MIN
                        maxYear.value = currentCalendarYear()
                        navigateSearchFilters(
                            SearchFilterSet(citySlug = filters.citySlug ?: "moskva"),
                        )
                        onLocationChanged()
                    },
                )
            }
        }
        Box(
            modifier = {
                property("min-height", "240px")
            },
            underneath = {
                when {
                    result == null -> {
                        CarsGridSkeleton()
                    }
                    result.cars.isNotEmpty() -> {
                        CarsGrid(
                            cars = result.cars,
                            carHref = { car ->
                                buildCarDetailUrl(
                                    carId = car.id,
                                    startDate = filters.startDate,
                                    endDate = filters.endDate,
                                )
                            },
                        )
                        PaginationBar(
                            currentPage = urlPageToUiIndex(filters.page),
                            totalPages = result.totalPages,
                            totalCount = result.totalCount,
                            pageSize = 9,
                            onPageChange = { uiIndex ->
                                navigatePage(uiIndexToUrlPage(uiIndex))
                            },
                        )
                    }
                    else -> {
                        Div({
                            classes("drivebit-search-results-empty")
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
            },
            overlay = {
                if (showPriceFilter) {
                    BoxOverlay {
                        PriceFilter(
                            minPrice = minPrice,
                            maxPrice = maxPrice,
                            resultsCount = result?.totalCount ?: 0,
                            onReset = {
                                minPrice.value = 0
                                maxPrice.value = 50000
                                navigateFilter {
                                    it.copy(dailyRateMin = null, dailyRateMax = null)
                                }
                                showPriceFilter = false
                            },
                            onViewResults = {
                                navigateFilter {
                                    it.copy(
                                        dailyRateMin = minPrice.value,
                                        dailyRateMax = maxPrice.value,
                                    )
                                }
                                showPriceFilter = false
                            },
                        )
                    }
                }
                if (showBrandFilter) {
                    BoxOverlay {
                        BrandModelFilter(
                            initialBrandId = filters.brandId,
                            initialBrandName = filters.brandName,
                            onBrandSelected = { brandId, brandName ->
                                navigateFilter {
                                    it.copy(
                                        brandId = brandId,
                                        brandName = brandName,
                                        brandSlug = cityNameToSlug(brandName),
                                        modelId = null,
                                        modelName = null,
                                        modelSlug = null,
                                        citySlug = null,
                                    )
                                }
                            },
                            onModelSelected = { modelId, modelName ->
                                navigateFilter {
                                    it.copy(
                                        modelId = modelId,
                                        modelName = modelName,
                                        modelSlug = cityNameToSlug(modelName),
                                    )
                                }
                                showBrandFilter = false
                            },
                            onReset = {
                                navigateFilter {
                                    it.copy(
                                        brandId = null,
                                        brandName = null,
                                        brandSlug = null,
                                        modelId = null,
                                        modelName = null,
                                        modelSlug = null,
                                        citySlug = it.citySlug ?: "moskva",
                                    )
                                }
                                showBrandFilter = false
                            },
                            onOk = { showBrandFilter = false },
                        )
                    }
                }
                if (showDriveTypeFilter) {
                    BoxOverlay {
                        DriveTypeFilter(
                            onDriveTypeSelected = { name, translate ->
                                navigateFilter {
                                    it.copy(driveType = name, driveTypeLabel = translate)
                                }
                                showDriveTypeFilter = false
                            },
                            onReset = {
                                navigateFilter {
                                    it.copy(driveType = null, driveTypeLabel = null)
                                }
                                showDriveTypeFilter = false
                            },
                        )
                    }
                }
                if (showBodyTypeFilter) {
                    BoxOverlay {
                        BodyTypeFilter(
                            onBodyTypeSelected = { name, translate ->
                                navigateFilter {
                                    it.copy(bodyType = name, bodyTypeLabel = translate)
                                }
                                showBodyTypeFilter = false
                            },
                            onReset = {
                                navigateFilter {
                                    it.copy(bodyType = null, bodyTypeLabel = null)
                                }
                                showBodyTypeFilter = false
                            },
                        )
                    }
                }
                if (showSeatsFilter) {
                    BoxOverlay {
                        SeatsFilter(
                            selectedSeatsMin = filters.seatsMin,
                            resultsCount = result?.totalCount ?: 0,
                            onSeatsMinSelected = { seatsMin ->
                                navigateFilter { it.copy(seatsMin = seatsMin) }
                                showSeatsFilter = false
                            },
                            onReset = {
                                navigateFilter { it.copy(seatsMin = null) }
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
                                navigateFilter { it.copy(yearMin = null, yearMax = null) }
                                showYearFilter = false
                            },
                            onViewResults = {
                                navigateFilter {
                                    it.copy(yearMin = minYear.value, yearMax = maxYear.value)
                                }
                                showYearFilter = false
                            },
                        )
                    }
                }
                if (showMileageFilter) {
                    BoxOverlay {
                        MileageFilter(
                            selectedMileageMin = filters.mileageMin,
                            onMileageMinSelected = { mileageMin ->
                                navigateFilter { it.copy(mileageMin = mileageMin) }
                                showMileageFilter = false
                            },
                            onReset = {
                                navigateFilter { it.copy(mileageMin = null) }
                                showMileageFilter = false
                            },
                        )
                    }
                }
            },
        )
    }
}

@Composable
private fun SearchPageHeadline(text: String) {
    Div({
        classes("drivebit-search-headline-wrap")
        style {
            width(100.percent)
            property("max-width", "1200px")
            property("margin", "0 auto")
            padding(0.px)
        }
    }) {
        H1({
            id("drivebit-page-headline")
            classes("drivebit-page-headline")
            style {
                property("margin", "0")
                fontSize(32.px)
                fontWeight("600")
                property("line-height", "1.25")
                color(CSSColors.Black)
                property("text-wrap", "balance")
            }
        }) {
            Text(text)
        }
    }
}
