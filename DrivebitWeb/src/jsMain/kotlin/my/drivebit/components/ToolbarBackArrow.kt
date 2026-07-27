package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun ToolbarBackArrow(
    title: String,
    onBackClick: () -> Unit,
    badges: List<String> = emptyList(),
) {
    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Row)
            alignItems(AlignItems.Center)
            gap(12.px)
            paddingTop(16.px)
            paddingBottom(16.px)
        }
    }) {
        Div({
            onClick { onBackClick() }
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.Center)
                width(36.px)
                height(36.px)
                borderRadius(50.percent)
                cursor("pointer")
                property("transition", "background-color 0.2s ease")
                property("user-select", "none")
            }
            onMouseOver {
                (it.currentTarget as? org.w3c.dom.HTMLElement)?.style?.backgroundColor = "#f0f0f0"
            }
            onMouseOut {
                (it.currentTarget as? org.w3c.dom.HTMLElement)?.style?.backgroundColor = "transparent"
            }
        }) {
            Span({
                style {
                    fontSize(20.px)
                    fontWeight(600)
                    color(CSSColors.Black)
                    lineHeight("1")
                }
            }) {
                Text("←")
            }
        }

        Div({
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Row)
                alignItems(AlignItems.Center)
                gap(8.px)
                flexWrap(FlexWrap.Wrap)
            }
        }) {
            Span({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    fontSize(CSSTypography.FontSize.xxxl)
                    fontWeight(CSSTypography.FontWeight.bold)
                    color(CSSColors.Black)
                    letterSpacing(0.5.px)
                    lineHeight("1.2")
                }
            }) {
                Text(title)
            }
            VerificationBadgeRow(badges)
        }
    }
}
