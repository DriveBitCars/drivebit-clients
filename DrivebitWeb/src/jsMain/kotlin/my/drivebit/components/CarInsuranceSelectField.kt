package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.network.services.CarInsuranceType
import org.jetbrains.compose.web.attributes.selected
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Select
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLSelectElement

@Composable
fun CarInsuranceSelectField(
    insurance: String?,
    insuranceTranslate: String?,
    onSelect: (String?) -> Unit,
) {
    val current = insurance.orEmpty()
    Column(gap = 12.px) {
        Span({
            style {
                applyTypography(CSSTypography.Styles.body)
                fontSize(CSSTypography.FontSize.sm)
                fontWeight(CSSTypography.FontWeight.medium)
                color(CSSColors.Black)
            }
        }) {
            Text("Страховка")
        }
        Select(
            attrs = {
                onChange { event ->
                    val v = (event.target as HTMLSelectElement).value
                    onSelect(v.takeIf { it.isNotBlank() })
                }
                style {
                    display(DisplayStyle.Block)
                    width(100.percent)
                    padding(12.px, 14.px)
                    fontSize(CSSTypography.FontSize.base)
                    border(1.px, LineStyle.Solid, CSSColors.Gray600)
                    borderRadius(8.px)
                }
            },
        ) {
            Option(
                value = "",
                attrs = {
                    if (current.isEmpty()) selected()
                },
            ) {
                Text("Не указано")
            }
            CarInsuranceType.optionsForUi.forEach { (api, label) ->
                val displayLabel =
                    if (current == api) {
                        insuranceTranslate?.takeIf { it.isNotBlank() } ?: label
                    } else {
                        label
                    }
                Option(
                    value = api,
                    attrs = {
                        if (current == api) selected()
                    },
                ) {
                    Text(displayLabel)
                }
            }
            val raw = insurance?.trim().orEmpty()
            if (raw.isNotEmpty() && raw !in CarInsuranceType.knownApiValues) {
                Option(
                    value = raw,
                    attrs = {
                        if (current == raw) selected()
                    },
                ) {
                    Text(insuranceTranslate?.takeIf { it.isNotBlank() } ?: raw)
                }
            }
        }
    }
}
