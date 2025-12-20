package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div

@Composable
fun StringList(
    strings: List<String>,
    onSelected: (String) -> Unit,
) {
    if (strings.isEmpty()) return

    Div({
        style {
            marginTop(8.px)
            backgroundColor(CSSColors.White)
            borderRadius(8.px)
            property("box-shadow", "0 2px 8px rgba(0, 0, 0, 0.1)")
            maxHeight(300.px)
            property("overflow-y", "auto")
            property("overflow-x", "hidden")
        }
    }) {
        strings.forEachIndexed { index, suggestion ->
            Div({
                onClick {
                    onSelected(suggestion)
                }
                style {
                    cursor("pointer")
                    property("transition", "background-color 0.2s ease")
                    padding(12.px, 16.px)
                    if (index < strings.size - 1) {
                        property("border-bottom", "1px solid ${CSSColors.Gray300String}")
                    }
                }
                onMouseEnter {
                    (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                        "background-color",
                        "rgba(0, 0, 0, 0.05)",
                    )
                }
                onMouseLeave {
                    (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                        "background-color",
                        "transparent",
                    )
                }
            }) {
                Item(
                    icon = null,
                    text = suggestion,
                    showDivider = false,
                )
            }
        }
    }
}
