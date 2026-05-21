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
import my.drivebit.components.Box
import my.drivebit.components.BoxOverlay
import my.drivebit.components.BrandModelFilter
import my.drivebit.components.CarsGrid
import my.drivebit.components.Column
import my.drivebit.components.DriveTypeFilter
import my.drivebit.components.FilterChip
import my.drivebit.components.FlowRow
import my.drivebit.components.Loader
import my.drivebit.components.PaginationBar
import my.drivebit.components.PriceFilter
import my.drivebit.components.SearchDateRangeSelector
import my.drivebit.components.SeatsFilter
import my.drivebit.components.TextError
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.navigation.NavigationState
import my.drivebit.repositories.CurrentFiltersRepository
import my.drivebit.web.BrandSlugResolver
import my.drivebit.web.navigateToCarDetail
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
                    val searchPageDateViewModel: SearchPageDateViewModel = koinInject()
                    val searchPageDateEndViewModel: SearchPageDateEndViewModel = koinInject()
                    Column(gap = 24.px) {
                        SearchDateRangeSelector(
                            startDate = searchParams.first,
                            endDate = searchParams.second,
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
                            Div({
                                onClick {
                                    navigationController.navigateTo("/search/ai")
                                }
                                style {
                                    padding(8.px, 12.px)
                                    backgroundColor(CSSColors.Blue)
                                    borderRadius(8.px)
                                    border(1.px, LineStyle.Solid, CSSColors.Blue)
                                    cursor("pointer")
                                    property("transition", "all 0.2s ease")
                                }
                            }) {
                                Span({
                                    style {
                                        fontSize(14.px)
                                        color(CSSColors.White)
                                        fontWeight("500")
                                    }
                                }) {
                                    Text("ИИ-поиск")
                                }
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
                                        onCarClick = { car ->
                                            navigateToCarDetail(
                                                carId = car.id,
                                                startDate = startDateByRepo ?: searchParams.first,
                                                endDate = endDateByRepo ?: searchParams.second,
                                                navigationController = navigationController,
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
                            },
                        )
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
