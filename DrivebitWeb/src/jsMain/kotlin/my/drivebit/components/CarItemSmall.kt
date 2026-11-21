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
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.left
import org.jetbrains.compose.web.css.lineHeight
import org.jetbrains.compose.web.css.opacity
import org.jetbrains.compose.web.css.overflow
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.rgba
import org.jetbrains.compose.web.css.right
import org.jetbrains.compose.web.css.top
import org.jetbrains.compose.web.css.bottom
import org.jetbrains.compose.web.css.width
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
    var suppressClickAfterSwipe by remember { mutableStateOf(false) }
    val isMobileViewport = window.innerWidth <= 768
    val imageLoadsEagerly = carGridImageLoadsEagerly(gridIndex, isMobileViewport)

    val classBadge = carClassBadgeLabel(car)

    val cardContent: @Composable () -> Unit = {
        Column(
            attrs = { classes("drivebit-car-card") },
            modifier = {
                width(100.percent)
                property("min-width", "0")
                property("max-width", "100%")
                property("box-sizing", "border-box")
                borderRadius(30.px)
                property("overflow", "hidden")
                backgroundColor(CSSColors.White)
                property("padding-top", "8px")
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
                    if (isMobileViewport && allPhotos.size > 1) {
                        onTouchStart { event ->
                            suppressClickAfterSwipe = false
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
                                val nextIndex =
                                    resolveCarPhotoSwipeIndex(
                                        currentIndex = safePhotoIndex,
                                        photoCount = allPhotos.size,
                                        startX = startX,
                                        endX = endX,
                                    )
                                if (nextIndex != safePhotoIndex) {
                                    currentPhotoIndex = nextIndex
                                    suppressClickAfterSwipe = true
                                    event.preventDefault()
                                    event.stopPropagation()
                                } else if (isCarPhotoSwipeSignificant(startX, endX)) {
                                    suppressClickAfterSwipe = true
                                    event.preventDefault()
                                    event.stopPropagation()
                                }
                            }
                            touchStartX = null
                        }
                    }
                    onClick { event ->
                        if (suppressClickAfterSwipe) {
                            suppressClickAfterSwipe = false
                            event.preventDefault()
                            event.stopPropagation()
                        }
                    }
                    style {
                        width(100.percent)
                        position(Position.Relative)
                        overflow("hidden")
                        property("touch-action", "pan-y")
                        property("margin", "0 9px")
                        borderRadius(24.px)
                        property("width", "calc(100% - 18px)")
                        property("aspect-ratio", "16 / 9")
                        property("background", "#e8eaef")
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
                            style {
                                width(100.percent)
                                height(100.percent)
                                property("object-fit", "cover")
                                property("object-position", "center")
                                display(DisplayStyle.Block)
                                position(Position.Absolute)
                                property("inset", "0")
                                if (href != null) {
                                    property("pointer-events", "none")
                                }
                            }
                        },
                    )
                    if (classBadge != null) {
                        Div({
                            style {
                                position(Position.Absolute)
                                left(10.px)
                                top(14.px)
                                backgroundColor(CSSColors.White)
                                borderRadius(25.px)
                                padding(6.px, 12.px)
                                fontSize(12.px)
                                fontWeight("500")
                                color(Color("#001845"))
                                property("pointer-events", "none")
                                property("z-index", "2")
                            }
                        }) {
                            Text(classBadge)
                        }
                    }

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
                        property("margin", "0 9px")
                        property("width", "calc(100% - 18px)")
                        property("aspect-ratio", "16 / 9")
                        borderRadius(24.px)
                        backgroundColor(CSSColors.Gray300)
                        color(CSSColors.Gray600)
                        fontSize(14.px)
                        property("box-sizing", "border-box")
                    },
                ) {
                    Text("Нет фото")
                }
            }
            Column(
                gap = 15.px,
                attrs = { classes("drivebit-car-card-body") },
                modifier = {
                    padding(20.px, 16.px)
                    property("width", "100%")
                    property("max-width", "100%")
                    property("min-width", "0")
                    property("box-sizing", "border-box")
                },
            ) {
                val carName = formatCarTitle(car)
                if (carName.isNotEmpty()) {
                    Div({
                        classes("drivebit-car-card-title")
                        style {
                            fontSize(17.px)
                            fontWeight("600")
                            color(Color("#152032"))
                            property("font-family", "\"Roboto\", sans-serif")
                            property("line-height", "1.2")
                            property("white-space", "nowrap")
                            property("overflow", "hidden")
                            property("text-overflow", "ellipsis")
                        }
                    }) {
                        Text(carName)
                    }
                }
                Div({
                    classes("drivebit-car-card-footer")
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        justifyContent(JustifyContent.SpaceBetween)
                        gap(10.px)
                        property("width", "100%")
                        property("min-width", "0")
                        property("flex-wrap", "nowrap")
                        property("box-sizing", "border-box")
                    }
                }) {
                    Div({
                        classes("drivebit-car-card-price")
                        style {
                            color(Color("#152032"))
                            property("font-family", "\"Inter Tight\", sans-serif")
                            property("font-weight", "600")
                            property("line-height", "1.2")
                            property("white-space", "nowrap")
                            property("min-width", "0")
                            property("flex", "1 1 auto")
                            property("overflow", "hidden")
                            property("text-overflow", "ellipsis")
                        }
                    }) {
                        val price = car.minDailyPrice()
                        if (price != null) {
                            Span({
                                classes("drivebit-car-card-price-main")
                                style {
                                    fontSize(24.px)
                                    fontWeight("600")
                                    color(Color("#152032"))
                                }
                            }) {
                                Text("от $price ₽")
                            }
                            Span({
                                classes("drivebit-car-card-price-unit")
                                style {
                                    fontSize(16.px)
                                    fontWeight("600")
                                    color(Color("#a1a6ad"))
                                }
                            }) {
                                Text("/сутки")
                            }
                        } else {
                            Span({
                                classes("drivebit-car-card-price-unit")
                                style {
                                    fontSize(16.px)
                                    color(Color("#a1a6ad"))
                                }
                            }) {
                                Text("Цена по запросу")
                            }
                        }
                    }
                    Span({
                        classes("drivebit-car-card-cta", "db-btn", "db-btn--md", "db-btn--primary")
                        style {
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                            justifyContent(JustifyContent.Center)
                            property("flex", "0 0 auto")
                            height(40.px)
                            property("min-width", "0")
                            padding(0.px, 14.px)
                            borderRadius(16.px)
                            backgroundColor(CSSColors.Blue)
                            color(CSSColors.White)
                            property("font-family", "\"Roboto\", sans-serif")
                            fontSize(14.px)
                            fontWeight("500")
                            property("white-space", "nowrap")
                            property("box-sizing", "border-box")
                        }
                    }) {
                        Text("Посмотреть")
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
                    property("max-width", "100%")
                    property("min-width", "0")
                    property("box-sizing", "border-box")
                    color(Color("#152032"))
                }
            },
        ) {
            cardContent()
        }
    } else {
        cardContent()
    }
}

private fun carClassBadgeLabel(car: CarItem): String? {
    // Hidden for now: badges were derived only from minDailyPrice
    // (<2500 Эконом, <4500 Комфорт, <8000 Бизнес, else Премиум).
    return null
}
