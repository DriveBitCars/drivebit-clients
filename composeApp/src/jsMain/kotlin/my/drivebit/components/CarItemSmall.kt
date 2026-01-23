package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import my.drivebit.network.services.CarItem
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text

@Composable
fun CarItemSmall(
    car: CarItem,
    onClick: (() -> Unit)? = null,
) {
    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            borderRadius(8.px)
            overflow("hidden")
            backgroundColor(CSSColors.White)
            onClick?.let {
                cursor("pointer")
            }
        }
        onClick?.let {
            onClick { it() }
        }
    }) {
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
            Div({
                style {
                    width(100.percent)
                    height(150.px)
                    backgroundColor(CSSColors.Gray300)
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    justifyContent(JustifyContent.Center)
                    color(CSSColors.Gray600)
                    fontSize(14.px)
                }
            }) {
                Text("Нет фото")
            }
        }
        Div({
            style {
                padding(12.px)
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                gap(4.px)
            }
        }) {
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
        }
    }
}
