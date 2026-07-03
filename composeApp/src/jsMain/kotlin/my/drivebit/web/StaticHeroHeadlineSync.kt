package my.drivebit.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

@Composable
fun StaticHeroHeadlineSync(currentPath: String) {
    LaunchedEffect(currentPath) {
        if (!shouldShowStaticHeroShell(currentPath)) return@LaunchedEffect
        if (document.getElementById("drivebit-hero-headline") == null) return@LaunchedEffect
        heroHeadlineForCityPath(currentPath)?.let { syncStaticHeroHeadlineText(it) }
    }
}

fun syncStaticHeroHeadlineText(headline: String) {
    val element = document.getElementById("drivebit-hero-headline") as? HTMLElement ?: return
    if (element.textContent != headline) {
        element.textContent = headline
    }
}
