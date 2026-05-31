package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import my.drivebit.shell.AppWithHeader
import my.drivebit.components.Column
import my.drivebit.components.TextError
import my.drivebit.design.CSSColors
import my.drivebit.utils.PHOTOS
import my.drivebit.utils.getUrlParameters
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text

@Composable
fun CarPhotosGalleryPage() {
    val photoUrls =
        remember {
            getUrlParameters(PHOTOS).filter { it.isNotBlank() }
        }

    AppWithHeader {
        Div({
            style {
                width(100.percent)
                property("max-width", "1200px")
                property("margin", "0 auto")
            }
        }) {
            if (photoUrls.isEmpty()) {
                TextError("Не указаны фотографии")
            } else {
                CarPhotosGalleryContent(photoUrls = photoUrls)
            }
        }
    }
}

@Composable
private fun CarPhotosGalleryContent(photoUrls: List<String>) {
    Column(gap = 24.px) {
        Div({
            style {
                fontSize(24.px)
                fontWeight("700")
                color(CSSColors.Black)
            }
        }) {
            Text("Фотографии")
        }

        Column(
            gap = 16.px,
            modifier = { width(100.percent) },
        ) {
            photoUrls.forEach { photoUrl ->
                Div({
                    style {
                        width(100.percent)
                        borderRadius(8.px)
                        overflow("hidden")
                        backgroundColor(CSSColors.White)
                    }
                }) {
                    Img(
                        src = photoUrl,
                        attrs = {
                            style {
                                width(100.percent)
                                property("height", "auto")
                                display(DisplayStyle.Block)
                            }
                        },
                    )
                }
            }
        }
    }
}
