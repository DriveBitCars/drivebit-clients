package my.drivebit.screens

import androidx.compose.runtime.Composable
import kotlinx.browser.window
import my.drivebit.design.CSSColors
import my.drivebit.utils.getUrlParameter
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.left
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.top
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.css.vw
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text

@Composable
fun InspectionActPhotoPage() {
    val photoUrl = getUrlParameter("url").trim()

    Div({
        style {
            position(Position.Fixed)
            property("inset", "0")
            width(100.vw)
            height(100.vh)
            backgroundColor(Color.black)
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.Center)
        }
    }) {
        Button({
            attr("type", "button")
            style {
                position(Position.Fixed)
                top(16.px)
                left(16.px)
                padding(10.px, 16.px)
                backgroundColor(CSSColors.White)
                color(CSSColors.Black)
                border(0.px)
                borderRadius(8.px)
                fontSize(14.px)
                fontWeight("600")
                cursor("pointer")
                property("z-index", "2")
            }
            onClick { window.history.back() }
        }) {
            Text("Назад")
        }
        if (photoUrl.isBlank()) {
            Div({
                style {
                    color(CSSColors.White)
                    fontSize(16.px)
                }
            }) {
                Text("Фото не указано")
            }
        } else {
            Img(
                src = photoUrl,
                attrs = {
                    style {
                        property("max-width", "100vw")
                        property("max-height", "100vh")
                        width(100.percent)
                        height(100.percent)
                        property("object-fit", "contain")
                    }
                },
            )
        }
    }
}
