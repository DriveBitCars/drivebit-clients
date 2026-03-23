package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text

@Composable
fun PriceFilterButtons(
    minPrice: MutableState<Int>,
    maxPrice: MutableState<Int>,
    onMinInputValueChange: (String) -> Unit,
    onMaxInputValueChange: (String) -> Unit,
    resultsCount: Int,
    onReset: () -> Unit,
    onViewResults: () -> Unit,
) {
    my.drivebit.components.Row(
        gap = 12.px,
        alignItems = AlignItems.Center,
    ) {
        Button({
            onClick {
                minPrice.value = 0
                maxPrice.value = 50000
                onMinInputValueChange("0")
                onMaxInputValueChange("50000+")
                onReset()
            }
            style {
                flex(1)
                padding(12.px, 24.px)
                borderRadius(8.px)
                backgroundColor(CSSColors.White)
                border(1.px, LineStyle.Solid, CSSColors.Gray300)
                color(CSSColors.Gray600)
                cursor("pointer")
                applyTypography(CSSTypography.Styles.button)
                fontSize(CSSTypography.FontSize.base)
                fontWeight(CSSTypography.FontWeight.medium)
                property("transition", "all 0.2s ease")
            }
            onMouseEnter {
                (it.target as org.w3c.dom.HTMLButtonElement).style.setProperty(
                    "background-color",
                    CSSColors.Gray300String,
                )
            }
            onMouseLeave {
                (it.target as org.w3c.dom.HTMLButtonElement).style.setProperty(
                    "background-color",
                    CSSColors.WhiteString,
                )
            }
        }) {
            Text("Сбросить")
        }

        Button({
            onClick { onViewResults() }
            style {
                flex(1)
                padding(12.px, 24.px)
                borderRadius(8.px)
                backgroundColor(CSSColors.Blue)
                border(0.px)
                color(CSSColors.White)
                cursor("pointer")
                applyTypography(CSSTypography.Styles.button)
                fontSize(CSSTypography.FontSize.base)
                fontWeight(CSSTypography.FontWeight.semibold)
                property("transition", "background-color 0.2s ease")
            }
            onMouseEnter {
                (it.target as org.w3c.dom.HTMLButtonElement).style.setProperty(
                    "background-color",
                    CSSColors.BlueRedString,
                )
            }
            onMouseLeave {
                (it.target as org.w3c.dom.HTMLButtonElement).style.setProperty(
                    "background-color",
                    CSSColors.BlueString,
                )
            }
        }) {
            Text("Показать")
        }
    }
}
