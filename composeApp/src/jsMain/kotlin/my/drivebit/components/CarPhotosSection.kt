package my.drivebit.components

import androidx.compose.runtime.Composable
import kotlinx.browser.window
import my.drivebit.components.ResponsiveContainer
import my.drivebit.design.CSSColors
import my.drivebit.network.services.CarDetailResponse
import my.drivebit.network.services.CarPhotoItem
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun CarPhotosSection(car: CarDetailResponse) {
    val allPhotos = (car.photos + (car.general?.photos ?: emptyList())).distinctBy { it.id }
    val mainPhoto = allPhotos.firstOrNull()
    val thumbnailPhotos = allPhotos.drop(1).take(3)

    ResponsiveContainer { isMobile ->
        Column(
            gap = 16.px,
        ) {
            if (isMobile) {
                CarMainPhoto(mainPhoto, car.id, allPhotos.isNotEmpty())
            } else {
                Row(
                    gap = 16.px,
                    modifier = { width(100.percent) },
                ) {
                    Column(
                        gap = 8.px,
                        modifier = { flex(1) },
                    ) {
                        CarMainPhoto(mainPhoto, car.id, allPhotos.isNotEmpty())
                    }

                    Column(
                        gap = 8.px,
                        modifier = { width(200.px) },
                    ) {
                        thumbnailPhotos.forEach { photo ->
                            CarThumbnail(photo, car.id, allPhotos.isNotEmpty())
                        }
                    }
                }
            }

            if (isShowButton(isMobile, allPhotos.size)) {
                Button({
                    style {
                        width(100.percent)
                        padding(12.px)
                        borderRadius(8.px)
                        border(1.px, LineStyle.Solid, CSSColors.Gray300)
                        backgroundColor(CSSColors.White)
                        cursor("pointer")
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        justifyContent(JustifyContent.Center)
                        gap(8.px)
                    }
                    onClick {
                        window.location.href = "/car-photos-gallery?id=${car.id}"
                    }
                }) {
                    Span({
                        style {
                            fontSize(14.px)
                            color(CSSColors.Gray600)
                        }
                    }) {
                        Text("Показать ${allPhotos.size} фотографий")
                    }
                }
            }
        }
    }
}

private fun isShowButton(
    isMobile: Boolean,
    photoSize: Int,
): Boolean {
    if (isMobile) {
        return photoSize > 1
    }
    return photoSize > 3
}

@Composable
private fun CarMainPhoto(
    mainPhoto: CarPhotoItem?,
    carId: String,
    isClickable: Boolean,
) {
    if (mainPhoto != null && mainPhoto.url.isNotEmpty()) {
        if (isClickable) {
            Div({
                style { cursor("pointer") }
                onClick { window.location.href = "/car-photos-gallery?id=$carId" }
            }) {
                Img(
                    src = mainPhoto.url,
                    attrs = {
                        style {
                            width(100.percent)
                            height(400.px)
                            property("object-fit", "cover")
                            borderRadius(8.px)
                        }
                    },
                )
            }
        } else {
            Img(
                src = mainPhoto.url,
                attrs = {
                    style {
                        width(100.percent)
                        height(400.px)
                        property("object-fit", "cover")
                        borderRadius(8.px)
                    }
                },
            )
        }
    } else {
        NoPhotoPlaceholder()
    }
}

@Composable
private fun CarThumbnail(
    photo: CarPhotoItem,
    carId: String,
    isClickable: Boolean,
) {
    Div({
        style {
            position(Position.Relative)
            if (isClickable) cursor("pointer")
        }
        if (isClickable) {
            onClick { window.location.href = "/car-photos-gallery?id=$carId" }
        }
    }) {
        Img(
            src = photo.url,
            attrs = {
                style {
                    width(100.percent)
                    height(120.px)
                    property("object-fit", "cover")
                    borderRadius(8.px)
                }
            },
        )
    }
}
