package my.drivebit.components

import androidx.compose.runtime.Composable
import kotlinx.browser.window
import my.drivebit.design.CSSColors
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Text

private const val DEALS_LINK_PHRASE = "Ожидается ваше подтверждение."

@Composable
fun MessageTextWithDealsLink(
    text: String,
    stopPropagation: Boolean = false,
) {
    if (text.isBlank()) return

    if (text.contains(DEALS_LINK_PHRASE)) {
        val before = text.substringBefore(DEALS_LINK_PHRASE)
        val after = text.substringAfter(DEALS_LINK_PHRASE)

        if (before.isNotBlank()) {
            Text(before)
        }
        A(attrs = {
            attr("href", "/my-deals")
            onClick { event ->
                event.preventDefault()
                if (stopPropagation) event.stopPropagation()
                window.location.href = "/my-deals"
            }
            style {
                color(CSSColors.Blue)
                property("text-decoration", "underline")
                cursor("pointer")
            }
        }) {
            Text(DEALS_LINK_PHRASE)
        }
        if (after.isNotBlank()) {
            Text(after)
        }
    } else {
        Text(text)
    }
}
