package my.drivebit.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

@Composable
fun StaticFiltersShellSync(currentPath: String) {
    LaunchedEffect(currentPath) {
        val nav = document.getElementById("drivebit-filters-static") as? HTMLElement ?: return@LaunchedEffect
        nav.style.display =
            if (isSearchPath(currentPath) || isMyCitySelectionPath(currentPath)) {
                "none"
            } else {
                ""
            }
    }
}
