package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.viewmodels.FilterItem
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.marginLeft
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun filterButton(
    filter: FilterItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    UniversalButton(
        isSelected = isSelected,
        onClick = onClick,
    ) {
        Img(
            src = filter.icon,
            alt = filter.icon,
            attrs = {
                style {
                    width(20.px)
                    height(20.px)
                    property("object-fit", "contain")
                    property("transition", "opacity 0.3s ease")
                    property("pointer-events", "none")
                    if (isSelected) {
                        property("filter", "brightness(0) invert(1)")
                    }
                }
            },
        )

        Span({
            style {
                applyTypography(CSSTypography.Styles.button)
                marginLeft(4.px)
                property("pointer-events", "none")
            }
        }) {
            Text(filter.title)
        }
    }
}
