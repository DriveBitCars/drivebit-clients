package my.drivebit.components

import androidx.compose.runtime.Composable
import kotlinx.browser.window
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Text

@Composable
fun TermsConsentCheckbox(
    accepted: Boolean,
    onAcceptedChange: (Boolean) -> Unit,
) {
    Div({
        style {
            display(DisplayStyle.Flex)
            alignItems(AlignItems.FlexStart)
            gap(12.px)
            marginTop(8.px)
        }
    }) {
        Input(
            type = InputType.Checkbox,
            attrs = {
                id("terms-consent-checkbox")
                checked(accepted)
                onInput { event ->
                    val checked =
                        (event.target as org.w3c.dom.HTMLInputElement).checked
                    onAcceptedChange(checked)
                }
                style {
                    width(20.px)
                    height(20.px)
                    marginTop(2.px)
                    cursor("pointer")
                    flexShrink(0)
                }
            },
        )
        Label(
            forId = "terms-consent-checkbox",
            attrs = {
                style {
                    cursor("pointer")
                    flexShrink(1)
                    applyTypography(CSSTypography.Styles.body)
                    fontSize(CSSTypography.FontSize.sm)
                    color(CSSColors.Black)
                    lineHeight("1.45")
                }
            },
        ) {
            Text("Согласен (-на) с ")
            TermsConsentLink(href = "/offer", label = "условиями оферты")
            Text(" и с ")
            TermsConsentLink(href = "/privacy", label = "обработкой персональных данных")
        }
    }
}

@Composable
private fun TermsConsentLink(
    href: String,
    label: String,
) {
    A(attrs = {
        attr("href", href)
        onClick { event ->
            event.preventDefault()
            window.location.href = href
        }
        style {
            color(CSSColors.Blue)
            property("text-decoration", "none")
            cursor("pointer")
        }
    }) {
        Text(label)
    }
}
