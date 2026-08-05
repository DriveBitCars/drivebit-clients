package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import my.drivebit.components.Column
import my.drivebit.design.CSSColors
import my.drivebit.network.services.CarItem
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun CarItemSmall(
    car: CarItem,
    href: String? = null,
    gridIndex: Int = 0,
) {
    var isImageHovered by remember { mutableStateOf(false) }
    var currentPhotoIndex by remember { mutableStateOf(0) }
    var touchStartX by remember { mutableStateOf<Double?>(null) }
    val isMobileViewport = window.innerWidth <= 768
    val imageLoadsEagerly = carGridImageLoadsEagerly(gridIndex, isMobileViewport)

    val cardContent: @Composable () -> Unit = {
        Column(
            modifier = {
                borderRadius(8.px)
                overflow("hidden")
                backgroundColor(CSSColors.White)
                if (href != null) {
                    cursor("pointer")
                }
            },
        ) {
            val photosFromGeneral = car.general.photos
            val photosFromTopLevel = car.photos
            val allPhotos =
                (photosFromGeneral + photosFromTopLevel)
                    .distinctBy { it.id }
                    .filter { it.previewUrl().isNotBlank() }
            val safePhotoIndex = currentPhotoIndex.coerceIn(0, (allPhotos.size - 1).coerceAtLeast(0))
            if (safePhotoIndex != currentPhotoIndex) {
                currentPhotoIndex = safePhotoIndex
            }
            val currentPhotoUrl = allPhotos.getOrNull(safePhotoIndex)?.previewUrl()

            if (!currentPhotoUrl.isNullOrEmpty()) {
                Div({
                    onMouseEnter { isImageHovered = true }
                    onMouseLeave { isImageHovered = false }
                    style {
                        width(100.percent)
                        height(150.px)
                        position(Position.Relative)
                        overflow("hidden")
                    }
                }) {
                    Img(
                        src = currentPhotoUrl,
                        attrs = {
                            attr("loading", carGridImageLoadingAttr(gridIndex, isMobileViewport))
                            attr("decoding", "async")
                            if (!imageLoadsEagerly) {
                                attr("fetchpriority", "low")
                            }
                            if (isMobileViewport) {
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
                                            swipeDelta > swipeThreshold && safePhotoIndex < allPhotos.lastIndex -> {
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
                            style {
                                width(100.percent)
                                height(150.px)
                                property("object-fit", "cover")
                                display(DisplayStyle.Block)
                                if (href != null) {
                                    property("pointer-events", "none")
                                }
                            }
                        },
                    )

                    if (allPhotos.size > 1 && (isImageHovered || isMobileViewport)) {
                        Div({
                            onClick { event ->
                                event.stopPropagation()
                                event.preventDefault()
                                if (safePhotoIndex > 0) {
                                    currentPhotoIndex = safePhotoIndex - 1
                                }
                            }
                            style {
                                position(Position.Absolute)
                                left(8.px)
                                top(50.percent)
                                property("transform", "translateY(-50%)")
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
                                property("pointer-events", "auto")
                                if (safePhotoIndex == 0) {
                                    opacity(0.35)
                                    cursor("default")
                                }
                            }
                        }) {
                            Text("<")
                        }

                        Div({
                            onClick { event ->
                                event.stopPropagation()
                                event.preventDefault()
                                if (safePhotoIndex < allPhotos.lastIndex) {
                                    currentPhotoIndex = safePhotoIndex + 1
                                }
                            }
                            style {
                                position(Position.Absolute)
                                right(8.px)
                                top(50.percent)
                                property("transform", "translateY(-50%)")
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
                                property("pointer-events", "auto")
                                if (safePhotoIndex == allPhotos.lastIndex) {
                                    opacity(0.35)
                                    cursor("default")
                                }
                            }
                        }) {
                            Text(">")
                        }
                    }

                    if (allPhotos.size > 1) {
                        Div({
                            style {
                                position(Position.Absolute)
                                bottom(8.px)
                                left(50.percent)
                                property("transform", "translateX(-50%)")
                                backgroundColor(rgba(0, 0, 0, 0.55))
                                color(CSSColors.White)
                                borderRadius(12.px)
                                padding(4.px, 8.px)
                                fontSize(12.px)
                                fontWeight("500")
                                lineHeight("1")
                                property("user-select", "none")
                                property("pointer-events", "none")
                            }
                        }) {
                            Text("${safePhotoIndex + 1} / ${allPhotos.size}")
                        }
                    }
                }
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

    if (href != null) {
        A(
            href = href,
            attrs = {
                style {
                    display(DisplayStyle.Block)
                    property("text-decoration", "none")
                    color(CSSColors.Black)
                }
            },
        ) {
            cardContent()
        }
    } else {
        cardContent()
    }
}
