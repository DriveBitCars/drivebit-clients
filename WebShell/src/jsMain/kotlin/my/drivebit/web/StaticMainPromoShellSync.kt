package my.drivebit.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

@Composable
fun StaticMainPromoShellSync(currentPath: String) {
    LaunchedEffect(currentPath) {
        val promo = document.querySelector(".drivebit-main-promo") as? HTMLElement ?: return@LaunchedEffect
        if (document.getElementById("drivebit-hero-static") == null) return@LaunchedEffect
        promo.style.display =
            if (shouldShowStaticHeroShell(currentPath)) {
                ""
            } else {
                "none"
            }
    }
}
