package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.components.Column
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextArea
import org.w3c.dom.HTMLTextAreaElement

@Composable
fun TextAreaField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    maxLength: Int? = null,
    rows: Int = 4,
) {
    Column(gap = 12.px) {
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

        TextArea(
            value = value,
            attrs = {
                maxLength?.let { attr("maxlength", it.toString()) }
                attr("rows", rows.toString())
                onInput { event ->
                    val newValue = (event.target as HTMLTextAreaElement).value
                    onValueChange(newValue)
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
