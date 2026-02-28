package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img

@Composable
fun FilterBackgroundImage(
    backgroundIconUrl: String,
    searchContent: (@Composable () -> Unit)? = null,
) {
    Div({
        style {
            width(100.percent)
            height(250.px)
            borderRadius(12.px)
            overflow("hidden")
            position(Position.Relative)
            marginBottom(30.px)
        }
        classes("filter-background")
    }) {
        Img(
            src = backgroundIconUrl,
            alt = "Filter background",
            attrs = {
                style {
                    width(100.percent)
                    height(100.percent)
                    display(DisplayStyle.Block)
                    property("object-fit", "cover")
                    property("object-position", "center")
                }
            },
        )

        // Градиентный оверлей для лучшей читаемости
        Div({
            style {
                position(Position.Absolute)
                top(0.px)
                left(0.px)
                right(0.px)
                bottom(0.px)
                background("linear-gradient(to bottom, rgba(0,0,0,0.1), rgba(0,0,0,0.3))")
            }
        })

        // Контент поиска по центру и сверху
        searchContent?.let {
            Div({
                style {
                    position(Position.Absolute)
                    top(90.px)
                    left(50.percent)
                    property("transform", "translateX(-50%)")
                    width(80.percent)
                    property("max-width", "600px")
                    property("z-index", "10")
                    display(DisplayStyle.Flex)
                    justifyContent(JustifyContent.Center)
                }
            }) {
                it()
            }
        }
    }
}
