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
                onInput { event ->
                    onValueChange((event.target as HTMLInputElement).value)
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
