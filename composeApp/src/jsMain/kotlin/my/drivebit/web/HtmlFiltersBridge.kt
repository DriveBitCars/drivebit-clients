package my.drivebit.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.browser.document
import kotlinx.browser.window
import my.drivebit.navigation.NavigationState
import my.drivebit.viewmodels.FilterItem
import my.drivebit.viewmodels.FiltersViewModel
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event

@Composable
fun HtmlFiltersBridge(
    filterViewModel: FiltersViewModel,
    navigationState: NavigationState,
) {
    if (remember {
            document.getElementById("drivebit-filters-static") == null ||
                document.getElementById("drivebit-hero-static") == null
        }
    ) {
        return
    }

    val filterState by filterViewModel.state.collectAsState()
    val currentPath by navigationState.currentPath.collectAsState()

    DisposableEffect(Unit) {
        val selectListener: (Event) -> Unit = listener@{ event ->
            val detail = event.asDynamic().detail
            val title = detail.title as? String ?: return@listener
            val path = detail.path as? String ?: return@listener

            val currentSelected = filterViewModel.state.value.selected
            val effectiveTitle =
                if (title != "Все" && title == currentSelected) "Все" else title
            val effectivePath =
                parseCitySlugFromPath(currentPath)?.let { citySlug ->
                    cityPathWithFilter(citySlug, effectiveTitle)
                } ?: path
            val effectiveUrl = effectivePath + window.location.search

            if (effectiveUrl != window.location.pathname + window.location.search) {
                window.history.pushState(null, "", effectiveUrl)
                navigationState.updatePath(effectivePath)
            }
            filterViewModel.onSelect(effectiveTitle)
            syncStaticFiltersPressedState(effectiveTitle)
        }

        val pathListener: (Event) -> Unit = {
            val path = window.location.pathname
            activeFilterTitleForCityPath(path)?.let { syncStaticFiltersPressedState(it) }
            heroHeadlineForCityPath(path)?.let { syncStaticHeroHeadlineText(it) }
        }

        window.addEventListener("drivebit-filter-select", selectListener)
        window.addEventListener("popstate", pathListener)
        js("window.__drivebitFiltersBridgeReady = true")
        onDispose {
            js("window.__drivebitFiltersBridgeReady = false")
            window.removeEventListener("drivebit-filter-select", selectListener)
            window.removeEventListener("popstate", pathListener)
        }
    }

    LaunchedEffect(currentPath, filterState.filters) {
        val activeFromPath = activeFilterTitleForCityPath(currentPath) ?: return@LaunchedEffect
        syncStaticFiltersPressedState(activeFromPath)
        syncStaticHeroBackground(activeFromPath, filterState.filters)
        window.dispatchEvent(org.w3c.dom.events.Event("drivebit-filter-path-changed"))
    }
}

private fun syncStaticHeroBackground(
    activeTitle: String,
    filters: List<FilterItem>,
) {
    val heroBg = document.getElementById("drivebit-hero-bg") as? HTMLImageElement ?: return
    val backgroundUrl = filters.firstOrNull { it.title == activeTitle }?.backgroundIcon ?: return
    if (heroBg.getAttribute("src") != backgroundUrl) {
        heroBg.setAttribute("src", backgroundUrl)
    }
}

private fun syncStaticFiltersPressedState(activeTitle: String) {
    val nav = document.getElementById("drivebit-filters-static") ?: return
    val buttons = nav.querySelectorAll(".drivebit-filter-btn")
    for (i in 0 until buttons.length) {
        val btn = buttons.item(i) as? HTMLElement ?: continue
        val title = btn.getAttribute("data-filter-title") ?: continue
        val pressed = title == activeTitle
        btn.setAttribute("aria-pressed", if (pressed) "true" else "false")
        if (pressed) {
            btn.classList.add("is-selected")
        } else {
            btn.classList.remove("is-selected")
        }
    }
}
