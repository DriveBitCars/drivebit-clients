package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLInputElement

@Composable
fun TextInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    maxLength: Int? = null,
    onFocus: (() -> Unit)? = null,
    onBlur: (() -> Unit)? = null,
    numeric: Boolean = false,
) {
    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            gap(12.px)
        }
    }) {
        Span({
            style {
                applyTypography(CSSTypography.Styles.body)
                fontSize(CSSTypography.FontSize.sm)
                fontWeight(CSSTypography.FontWeight.medium)
                color(CSSColors.Black)
            }
        }) {
            Text(label)
        }

        Input(
            type = InputType.Text,
            attrs = {
                value(value)
                maxLength?.let { attr("maxlength", it.toString()) }
                if (numeric) {
                    attr("inputmode", "numeric")
                    attr("pattern", "[0-9]*")
                }
                onInput { event ->
                    val newValue = (event.target as HTMLInputElement).value
                    if (numeric) {
                        val digitsOnly = newValue.filter { it.isDigit() }
                        onValueChange(digitsOnly)
                    } else {
                        onValueChange(newValue)
                    }
                }
                onFocus?.let { handler ->
                    onFocus { handler() }
                }
                onBlur?.let { handler ->
                    onBlur { handler() }
                }
                style {
                    applyTypography(CSSTypography.Styles.body)
                    fontSize(CSSTypography.FontSize.base)
                    color(CSSColors.Black)
                    border(1.px, LineStyle.Solid, CSSColors.Gray600)
                    borderRadius(8.px)
                    padding(12.px, 16.px)
                    width(100.percent)
                }
            },
        )
    }
}
