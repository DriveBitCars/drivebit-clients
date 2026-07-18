package my.drivebit.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import my.drivebit.utils.SearchUrlParts
import my.drivebit.utils.buildSearchUrl
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject
import org.w3c.dom.events.Event

@Composable
fun SearchApp() {
    val viewModel: SearchViewModel = koinInject()
    var locationHref by remember { mutableStateOf(window.location.pathname + window.location.search) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        val onPopState: (Event) -> Unit = {
            locationHref = window.location.pathname + window.location.search
        }
        window.addEventListener("popstate", onPopState)
    }

    LaunchedEffect(locationHref) {
        viewModel.load(locationHref)
    }

    Div({
        classes("drivebit-cars-grid-mounted")
        style { padding(16.px) }
    }) {
        H1 { Text("Поиск автомобилей") }
        when (val s = state) {
            is SearchUiState.Loading -> P { Text("Загрузка…") }
            is SearchUiState.Error -> P { Text(s.message) }
            is SearchUiState.Results -> {
                P {
                    Text(
                        "Найдено: ${s.result.totalCount}. Фильтры: " +
                            listOfNotNull(
                                s.filters.brandName,
                                s.filters.modelName,
                                s.filters.startDate,
                                s.filters.seatsMin?.let { "$it мест" },
                            ).joinToString(", ").ifEmpty { "все" },
                    )
                }
                    s.result.cars.forEach { car ->
                    val title =
                        listOf(car.general.brandName, car.general.modelName)
                            .filter { it.isNotEmpty() }
                            .joinToString(" ")
                            .ifEmpty { car.id }
                    val price = car.minDailyPrice()
                    Div { Text("$title${price?.let { " — $it ₽" } ?: ""}") }
                }
                if (s.result.totalPages > 1) {
                    Button({
                        onClick {
                            val nextPage = (s.filters.page + 1).coerceAtMost(s.result.totalPages)
                            navigateSearch(s.filters.copy(page = nextPage))
                            locationHref = window.location.pathname + window.location.search
                        }
                    }) { Text("Следующая страница") }
                }
                Button({
                    onClick {
                        navigateSearch(
                            applySearchFilterChange(s.filters) { it.copy(seatsMin = 5) },
                        )
                        locationHref = window.location.pathname + window.location.search
                    }
                }) { Text("5+ мест") }
            }
        }
    }
}

private fun navigateSearch(filters: SearchFilterSet) {
    val parts =
        SearchUrlParts(
            citySlug = filters.citySlug ?: if (filters.brandSlug == null) "moskva" else null,
            brandSlug = filters.brandSlug,
            modelSlug = filters.modelSlug,
            startDate = filters.startDate,
            endDate = filters.endDate,
            dailyRateMin = filters.dailyRateMin,
            dailyRateMax = filters.dailyRateMax,
            driveType = filters.driveType,
            driveTypeLabel = filters.driveTypeLabel,
            bodyType = filters.bodyType,
            bodyTypeLabel = filters.bodyTypeLabel,
            seatsMin = filters.seatsMin,
            yearMin = filters.yearMin,
            yearMax = filters.yearMax,
            mileageMin = filters.mileageMin,
            page = filters.page,
        )
    val url = buildSearchUrl(parts)
    window.history.pushState(null, "", url)
}
