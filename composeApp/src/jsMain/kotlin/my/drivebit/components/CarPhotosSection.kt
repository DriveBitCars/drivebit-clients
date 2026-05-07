package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import org.w3c.dom.events.Event

@Composable
fun CarPhotosSection(car: CarDetailResponse) {
    val photosFromGeneral = car.general?.photos ?: emptyList()
    val photosFromTopLevel = car.photos
    val allPhotos = (photosFromGeneral + photosFromTopLevel).distinctBy { it.id }
    val mainPhoto = allPhotos.firstOrNull()
    val thumbnailPhotos = allPhotos.drop(1).take(3)

    ResponsiveContainer { isMobile ->
        Column(
            gap = 16.px,
        ) {
            if (isMobile) {
                val mobilePhotos = allPhotos.filter { it.url.isNotBlank() }
                CarMobileMainPhotoCarousel(
                    carId = car.id,
                    photos = mobilePhotos,
                    galleryClickEnabled = allPhotos.isNotEmpty(),
                )
            } else {
                Row(
                    gap = 16.px,
                    alignItems = AlignItems.FlexStart,
                    modifier = { width(100.percent) },
                ) {
                    Column(
                        gap = 8.px,
                        modifier = {
                            flex(1)
                            minWidth(0.px)
                            alignItems(AlignItems.FlexStart)
                        },
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

            if (allPhotos.isNotEmpty()) {
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

private fun StyleScope.carMainPhotoImgStyle() {
    width(100.percent)
    property("height", "auto")
    property("max-width", "100%")
    property("flex-shrink", "0")
    display(DisplayStyle.Block)
    borderRadius(8.px)
}

@Composable
private fun CarMobileMainPhotoCarousel(
    carId: String,
    photos: List<CarPhotoItem>,
    galleryClickEnabled: Boolean,
) {
    var isImageHovered by remember { mutableStateOf(false) }
    var isMobileViewport by remember { mutableStateOf(window.innerWidth <= 768) }
    var currentPhotoIndex by remember(carId) { mutableStateOf(0) }
    var touchStartX by remember { mutableStateOf<Double?>(null) }

    DisposableEffect(Unit) {
        val listener: (Event) -> Unit = {
            isMobileViewport = window.innerWidth <= 768
        }
        window.addEventListener("resize", listener)
        onDispose {
            window.removeEventListener("resize", listener)
        }
    }

    if (photos.isEmpty()) {
        NoPhotoPlaceholder()
        return
    }

    val safePhotoIndex = currentPhotoIndex.coerceIn(0, photos.lastIndex)
    if (safePhotoIndex != currentPhotoIndex) {
        currentPhotoIndex = safePhotoIndex
    }
    val currentPhotoUrl = photos[safePhotoIndex].url

    Div({
        onMouseEnter { isImageHovered = true }
        onMouseLeave { isImageHovered = false }
        style {
            position(Position.Relative)
            width(100.percent)
            display(DisplayStyle.Block)
            overflow("hidden")
        }
    }) {
        Img(
            src = currentPhotoUrl,
            attrs = {
                if (photos.size > 1 && isMobileViewport) {
                    onTouchStart { event ->
                        touchStartX =
                            event.touches
                                .item(0)
                                ?.clientX
                                ?.toDouble()
                    }
                    onTouchEnd { event ->
                        val startX = touchStartX
                        val endX =
                            event.changedTouches
                                .item(0)
                                ?.clientX
                                ?.toDouble()
                        if (startX != null && endX != null) {
                            val swipeDelta = startX - endX
                            val swipeThreshold = 40.0
                            when {
                                swipeDelta > swipeThreshold && safePhotoIndex < photos.lastIndex -> {
                                    currentPhotoIndex = safePhotoIndex + 1
                                }

                                swipeDelta < -swipeThreshold && safePhotoIndex > 0 -> {
                                    currentPhotoIndex = safePhotoIndex - 1
                                }
                            }
                        }
                        touchStartX = null
                    }
                }
                if (galleryClickEnabled) {
                    onClick {
                        window.location.href = "/car-photos-gallery?id=$carId"
                    }
                }
                style {
                    carMainPhotoImgStyle()
                    if (galleryClickEnabled) {
                        cursor("pointer")
                    }
                }
            },
        )

        if (photos.size > 1 && (isImageHovered || isMobileViewport)) {
            Div({
                onClick {
                    it.stopPropagation()
                    if (safePhotoIndex > 0) {
                        currentPhotoIndex = safePhotoIndex - 1
                    }
                }
                style {
                    position(Position.Absolute)
                    left(8.px)
                    top(50.percent)
                    property("transform", "translateY(-50%)")
                    property("z-index", "2")
                    width(32.px)
                    height(32.px)
                    borderRadius(50.percent)
                    backgroundColor(rgba(0, 0, 0, 0.5))
                    color(CSSColors.White)
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    justifyContent(JustifyContent.Center)
                    fontSize(18.px)
                    cursor("pointer")
                    property("user-select", "none")
                    if (safePhotoIndex == 0) {
                        opacity(0.35)
                        cursor("default")
                    }
                }
            }) {
                Text("<")
            }

            Div({
                onClick {
                    it.stopPropagation()
                    if (safePhotoIndex < photos.lastIndex) {
                        currentPhotoIndex = safePhotoIndex + 1
                    }
                }
                style {
                    position(Position.Absolute)
                    right(8.px)
                    top(50.percent)
                    property("transform", "translateY(-50%)")
                    property("z-index", "2")
                    width(32.px)
                    height(32.px)
                    borderRadius(50.percent)
                    backgroundColor(rgba(0, 0, 0, 0.5))
                    color(CSSColors.White)
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    justifyContent(JustifyContent.Center)
                    fontSize(18.px)
                    cursor("pointer")
                    property("user-select", "none")
                    if (safePhotoIndex == photos.lastIndex) {
                        opacity(0.35)
                        cursor("default")
                    }
                }
            }) {
                Text(">")
            }
        }

        if (photos.size > 1) {
            Div({
                style {
                    position(Position.Absolute)
                    bottom(8.px)
                    left(50.percent)
                    property("transform", "translateX(-50%)")
                    property("z-index", "2")
                    backgroundColor(rgba(0, 0, 0, 0.55))
                    color(CSSColors.White)
                    borderRadius(12.px)
                    padding(4.px, 8.px)
                    fontSize(12.px)
                    fontWeight("500")
                    lineHeight("1")
                    property("user-select", "none")
                }
            }) {
                Text("${safePhotoIndex + 1} / ${photos.size}")
            }
        }
    }
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
                style {
                    cursor("pointer")
                    width(100.percent)
                    display(DisplayStyle.Block)
                }
                onClick { window.location.href = "/car-photos-gallery?id=$carId" }
            }) {
                Img(
                    src = mainPhoto.url,
                    attrs = {
                        style { carMainPhotoImgStyle() }
                    },
                )
            }
        } else {
            Img(
                src = mainPhoto.url,
                attrs = {
                    style { carMainPhotoImgStyle() }
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
