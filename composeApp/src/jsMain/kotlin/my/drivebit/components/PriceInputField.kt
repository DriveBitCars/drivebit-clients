package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun PriceInputField(
    label: String,
    value: String,
    onValueChange: (Int) -> Unit,
    minValue: Int = 0,
    maxValue: Int = 10000,
) {
    Column(gap = 8.px, modifier = { flex(1) }) {
        Span({
            style {
                applyTypography(CSSTypography.Styles.body)
                fontSize(CSSTypography.FontSize.sm)
                fontWeight(CSSTypography.FontWeight.medium)
                color(CSSColors.Gray600)
            }
        }) {
            Text(label)
        }
        Input(
            type = InputType.Number,
            attrs = {
                this.value(value.replace("+", ""))
                onInput { event ->
                    val newValue = (event.target as org.w3c.dom.HTMLInputElement).value
                    val numValue = newValue.toIntOrNull() ?: 0
                    if (numValue >= minValue && numValue <= maxValue) {
                        onValueChange(numValue)
                    }
                }
                style {
                    width(100.percent)
                    padding(12.px, 16.px)
                    borderRadius(8.px)
                    property("box-sizing", "border-box")
                    property("border", "1px solid ${CSSColors.Gray300String}")
                    property("font-size", "14px")
                    property("outline", "none")
                    property("transition", "border-color 0.2s ease")
                }
                onFocus {
                    (it.target as org.w3c.dom.HTMLInputElement).style.setProperty(
                        "border-color",
                        CSSColors.BlueString,
                    )
                }
                onBlur {
                    (it.target as org.w3c.dom.HTMLInputElement).style.setProperty(
                        "border-color",
                        CSSColors.Gray300String,
                    )
                }
            },
        )
    }
}
