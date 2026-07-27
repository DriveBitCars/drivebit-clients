package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.components.Column
import my.drivebit.components.Row
import my.drivebit.design.CSSColors
import org.jetbrains.compose.web.css.AlignItems
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

    Column(gap = 16.px) {
        if (carName.isNotEmpty()) {
            Row(
                gap = 8.px,
                alignItems = AlignItems.Center,
            ) {
                Div({
                    style {
                        fontSize(32.px)
                        fontWeight("700")
                        color(CSSColors.Black)
                    }
                }) {
                    Text(carName)
                }
                VerificationBadgeRow(verificationLabels)
            }
        }

        CarYearLabel(carYear)
    }
}
