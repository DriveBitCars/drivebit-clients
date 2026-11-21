package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.components.Row
import my.drivebit.design.CSSColors
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.FlexWrap
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun CarTitleSection(
    carName: String,
    carYear: Int,
    verificationLabels: List<String> = emptyList(),
) {
    if (carName.isEmpty() && carYear <= 0) return

    Row(
        gap = 10.px,
        flexWrap = FlexWrap.Wrap,
        alignItems = AlignItems.Center,
    ) {
        if (carName.isNotEmpty()) {
            Div({
                classes("db-car-title")
                style {
                    color(CSSColors.Black)
                    fontWeight("700")
                    fontSize(28.px)
                    property("font-family", "var(--db-font-display, Inter Tight, Roboto, sans-serif)")
                    property("letter-spacing", "-0.02em")
                    property("line-height", "120%")
                }
            }) {
                Text(carName)
            }
        }
        CarYearLabel(carYear)
        VerificationBadgeRow(verificationLabels)
    }
}
