package my.drivebit.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.document
import kotlinx.browser.window
import my.drivebit.components.DateRangeCalendarDialog
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.repositories.CurrentFiltersRepository
import my.drivebit.viewmodels.DateFieldViewModel
import org.w3c.dom.events.Event

@Composable
fun HtmlHeroDatesBridge(
    currentFiltersRepository: CurrentFiltersRepository,
    startDateViewModel: DateFieldViewModel,
    endDateViewModel: DateFieldViewModel,
) {
    if (remember { document.getElementById("drivebit-hero-static") == null }) {
        return
    }

    val navigationController = LocalNavigationController.current
    val startState by startDateViewModel.state.collectAsState()
    val endState by endDateViewModel.state.collectAsState()
    var locationSearch by remember { mutableStateOf(window.location.search) }

    DisposableEffect(Unit) {
        val datesChangedListener: (Event) -> Unit = {
            locationSearch = window.location.search
        }
        val openStartListener: (Event) -> Unit = {
            startDateViewModel.openCalendar()
        }
        val openEndListener: (Event) -> Unit = {
            endDateViewModel.openCalendar()
        }
        val searchListener: (Event) -> Unit = {
            val start = startDateViewModel.state.value.date
            val end = endDateViewModel.state.value.date
            writeHeroDatesToQuery(start, end)
            val url = buildHeroSearchUrl(start, end)
            navigationController?.navigateTo(url) ?: run { window.location.href = url }
        }
        window.addEventListener("drivebit-hero-dates-changed", datesChangedListener)
        window.addEventListener("popstate", datesChangedListener)
        window.addEventListener("drivebit-hero-open-start-calendar", openStartListener)
        window.addEventListener("drivebit-hero-open-end-calendar", openEndListener)
        window.addEventListener("drivebit-hero-search", searchListener)
        js("window.__drivebitHeroBridgeReady = true")
        onDispose {
            js("window.__drivebitHeroBridgeReady = false")
            window.removeEventListener("drivebit-hero-dates-changed", datesChangedListener)
            window.removeEventListener("popstate", datesChangedListener)
            window.removeEventListener("drivebit-hero-open-start-calendar", openStartListener)
            window.removeEventListener("drivebit-hero-open-end-calendar", openEndListener)
            window.removeEventListener("drivebit-hero-search", searchListener)
        }
    }

    LaunchedEffect(locationSearch) {
        val (start, end) = parseHeroDatesFromQuery(locationSearch)
        if (startState.date != start) {
            startDateViewModel.setDate(start)
        }
        if (endState.date != end) {
            endDateViewModel.setDate(end)
        }
    }

    LaunchedEffect(startState.date) {
        val currentStart = startState.date
        val currentEnd = endState.date
        if (currentStart != null && currentEnd != null && currentStart > currentEnd) {
            endDateViewModel.setDate(null)
        }
    }

    LaunchedEffect(endState.date) {
        val currentStart = startState.date
        val currentEnd = endState.date
        if (currentStart != null && currentEnd != null && currentEnd < currentStart) {
            startDateViewModel.setDate(null)
        }
    }

    LaunchedEffect(startState.date, endState.date) {
        updateHeroDateDomDisplays(startState.date, endState.date)
        currentFiltersRepository.updateStartDate(startState.date)
        currentFiltersRepository.updateEndDate(endState.date)
    }

    DateRangeCalendarDialog(
        startDateViewModel = startDateViewModel,
        endDateViewModel = endDateViewModel,
        onConfirm = {
            writeHeroDatesToQuery(
                startDateViewModel.state.value.date,
                endDateViewModel.state.value.date,
            )
        },
    )
}
