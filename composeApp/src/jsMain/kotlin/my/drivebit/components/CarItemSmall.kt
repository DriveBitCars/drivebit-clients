package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.components.Column
import my.drivebit.design.CSSColors
import my.drivebit.network.services.CarItem
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun CarItemSmall(
    car: CarItem,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = {
            borderRadius(8.px)
            overflow("hidden")
            backgroundColor(CSSColors.White)
            onClick?.let {
                cursor("pointer")
            }
        },
        attrs = {
            onClick?.let { handler ->
                onClick { handler() }
            }
        },
    ) {
        val photosFromGeneral = car.general?.photos ?: emptyList()
        val photosFromTopLevel = car.photos
        val allPhotos = (photosFromGeneral + photosFromTopLevel).distinctBy { it.id }
        val firstPhotoUrl = allPhotos.firstOrNull()?.url
        if (!firstPhotoUrl.isNullOrEmpty()) {
            Img(
                src = firstPhotoUrl,
                attrs = {
                    style {
                        width(100.percent)
                        height(150.px)
                        property("object-fit", "cover")
                    }
                },
            )
        } else {
            Row(
                alignItems = AlignItems.Center,
                justifyContent = JustifyContent.Center,
                modifier = {
                    width(100.percent)
                    height(150.px)
                    backgroundColor(CSSColors.Gray300)
                    color(CSSColors.Gray600)
                    fontSize(14.px)
                },
            ) {
                Text("Нет фото")
            }
        }
        Column(
            gap = 4.px,
            modifier = {
                padding(12.px)
            },
        ) {
            val carName = formatCarTitle(car)

            if (carName.isNotEmpty()) {
                Div({
                    style {
                        fontSize(16.px)
                        fontWeight("600")
                        color(CSSColors.Black)
                    }
                }) {
                    Text(carName)
                }
            }
            Div({
                style {
                    marginTop(4.px)
                    fontSize(14.px)
                    fontWeight("600")
                    color(CSSColors.Black)
                }
            }) {
                val price = car.minDailyPrice()
                if (price != null) {
                    Text("от $price ₽ ")
                    Span({
                        style {
                            fontWeight("400")
                            color(CSSColors.Gray600)
                        }
                    }) {
                        Text("/ сутки")
                    }
                } else {
                    Span({
                        style {
                            fontWeight("400")
                            color(CSSColors.Gray600)
                        }
                    }) {
                        Text("Цена по запросу")
                    }
                }
            }
        }
    }
}
