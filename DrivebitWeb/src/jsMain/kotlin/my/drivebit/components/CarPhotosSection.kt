package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import my.drivebit.design.CSSColors
import my.drivebit.network.services.CarDetailResponse
import my.drivebit.network.services.CarPhotoItem
import my.drivebit.utils.buildCarPhotosGalleryUrl
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.Event

private const val THUMBNAIL_HEIGHT_PX = 120
private const val THUMBNAIL_GAP_PX = 8
private const val THUMBNAIL_SCROLL_STEP_PX = THUMBNAIL_HEIGHT_PX + THUMBNAIL_GAP_PX

@Composable
fun CarPhotosSection(car: CarDetailResponse) {
    val photosFromGeneral = car.general?.photos ?: emptyList()
    val photosFromTopLevel = car.photos
    val allPhotos = (photosFromGeneral + photosFromTopLevel).distinctBy { it.id }
    val galleryPhotoUrls = allPhotos.mapNotNull { it.url.takeIf { url -> url.isNotBlank() } }
    val mainPhoto = allPhotos.firstOrNull()
    val thumbnailPhotos = allPhotos.drop(1)

    ResponsiveContainer { isMobile ->
        var mainPhotoHeightPx by remember { mutableStateOf(0) }

        Column(
            gap = 16.px,
        ) {
            if (isMobile) {
                val mobilePhotos = allPhotos.filter { it.url.isNotBlank() }
                CarMobileMainPhotoCarousel(
                    photos = mobilePhotos,
                    galleryPhotoUrls = galleryPhotoUrls,
                    galleryClickEnabled = allPhotos.isNotEmpty(),
                )
            } else {
                Row(
                    gap = 16.px,
                    alignItems = AlignItems.Stretch,
                    modifier = { width(100.percent) },
                ) {
                    Div({
                        ref { element ->
                            fun syncHeight() {
                                val height = element.offsetHeight
                                if (height > 0) {
                                    mainPhotoHeightPx = height
                                }
                            }
                            syncHeight()
                            val img = element.querySelector("img")
                            val onImgLoad: (Event) -> Unit = { syncHeight() }
                            img?.addEventListener("load", onImgLoad)
                            val onWindowResize: (Event) -> Unit = { syncHeight() }
                            window.addEventListener("resize", onWindowResize)
                            val rafId =
                                window.requestAnimationFrame {
                                    syncHeight()
                                    window.requestAnimationFrame { syncHeight() }
                                }
                            onDispose {
                                img?.removeEventListener("load", onImgLoad)
                                window.removeEventListener("resize", onWindowResize)
                                window.cancelAnimationFrame(rafId)
                            }
                        }
                        style {
                            flex(1)
                            minWidth(0.px)
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Column)
                            alignItems(AlignItems.FlexStart)
                        }
                    }) {
                        CarMainPhoto(mainPhoto, galleryPhotoUrls, allPhotos.isNotEmpty())
                    }

                    if (thumbnailPhotos.isNotEmpty()) {
                        CarThumbnailScroller(
                            photos = thumbnailPhotos,
                            galleryPhotoUrls = galleryPhotoUrls,
                            isClickable = allPhotos.isNotEmpty(),
                            heightPx = mainPhotoHeightPx,
                        )
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
                        window.location.href = buildCarPhotosGalleryUrl(galleryPhotoUrls)
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
    photos: List<CarPhotoItem>,
    galleryPhotoUrls: List<String>,
    galleryClickEnabled: Boolean,
) {
    var isImageHovered by remember { mutableStateOf(false) }
    var isMobileViewport by remember { mutableStateOf(window.innerWidth <= 768) }
    var currentPhotoIndex by remember(galleryPhotoUrls) { mutableStateOf(0) }
    var touchStartX by remember { mutableStateOf<Double?>(null) }
    var suppressClickAfterSwipe by remember { mutableStateOf(false) }
    var photoAreaHeightPx by remember { mutableStateOf(0) }
    var isPhotoLoading by remember { mutableStateOf(false) }

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

    LaunchedEffect(safePhotoIndex) {
        if (photoAreaHeightPx > 0) {
            isPhotoLoading = true
        }
    }

    Div({
        onMouseEnter { isImageHovered = true }
        onMouseLeave { isImageHovered = false }
        if (photos.size > 1 && isMobileViewport) {
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
                            photoCount = photos.size,
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
        if (galleryClickEnabled) {
            onClick {
                if (suppressClickAfterSwipe) {
                    suppressClickAfterSwipe = false
                    return@onClick
                }
                window.location.href = buildCarPhotosGalleryUrl(galleryPhotoUrls)
            }
        }
        style {
            position(Position.Relative)
            width(100.percent)
            display(DisplayStyle.Block)
            overflow("hidden")
            property("touch-action", "pan-y")
            if (photoAreaHeightPx > 0) {
                height(photoAreaHeightPx.px)
            }
            if (galleryClickEnabled) {
                cursor("pointer")
            }
        }
    }) {
        if (isPhotoLoading && photoAreaHeightPx > 0) {
            CarPhotoSwipePlaceholder(photoAreaHeightPx)
        }

        Img(
            src = currentPhotoUrl,
            attrs = {
                ref { element ->
                    fun onLoad() {
                        val height = element.offsetHeight
                        if (height > 0 && photoAreaHeightPx == 0) {
                            photoAreaHeightPx = height
                        }
                        isPhotoLoading = false
                    }
                    val loadListener: (Event) -> Unit = { onLoad() }
                    element.addEventListener("load", loadListener)
                    if (element.complete && element.naturalHeight > 0) {
                        onLoad()
                    }
                    onDispose {
                        element.removeEventListener("load", loadListener)
                    }
                }
                style {
                    carMainPhotoImgStyle()
                    property("pointer-events", "none")
                    if (photoAreaHeightPx > 0) {
                        height(photoAreaHeightPx.px)
                        property("object-fit", "cover")
                    }
                    if (isPhotoLoading && photoAreaHeightPx > 0) {
                        opacity(0)
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
private fun CarPhotoSwipePlaceholder(heightPx: Int) {
    Div({
        style {
            position(Position.Absolute)
            top(0.px)
            left(0.px)
            width(100.percent)
            height(heightPx.px)
            backgroundColor(CSSColors.Gray300)
            borderRadius(8.px)
            property("z-index", "0")
        }
    })
}

@Composable
private fun CarMainPhoto(
    mainPhoto: CarPhotoItem?,
    galleryPhotoUrls: List<String>,
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
                onClick { window.location.href = buildCarPhotosGalleryUrl(galleryPhotoUrls) }
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
private fun CarThumbnailScroller(
    photos: List<CarPhotoItem>,
    galleryPhotoUrls: List<String>,
    isClickable: Boolean,
    heightPx: Int,
) {
    var scrollElement by remember { mutableStateOf<HTMLDivElement?>(null) }
    var canScrollUp by remember { mutableStateOf(false) }
    var canScrollDown by remember { mutableStateOf(false) }
    var showArrows by remember { mutableStateOf(false) }

    fun updateScrollState() {
        val el = scrollElement ?: return
        val overflows = el.scrollHeight > el.clientHeight + 1
        showArrows = overflows
        canScrollUp = el.scrollTop > 0
        canScrollDown = el.scrollTop + el.clientHeight < el.scrollHeight - 1
    }

    LaunchedEffect(photos.size, scrollElement, heightPx) {
        updateScrollState()
    }

    Div({
        style {
            position(Position.Relative)
            width(200.px)
            flexShrink(0)
            if (heightPx > 0) {
                height(heightPx.px)
            } else {
                alignSelf(AlignSelf.Stretch)
                minHeight(0.px)
            }
            overflow("hidden")
        }
    }) {
        Div({
            ref { element ->
                scrollElement = element
                updateScrollState()
                onDispose {
                    if (scrollElement === element) {
                        scrollElement = null
                    }
                }
            }
            onScroll { updateScrollState() }
            style {
                position(Position.Absolute)
                top(0.px)
                left(0.px)
                right(0.px)
                bottom(0.px)
                overflowY("auto")
                property("scrollbar-width", "thin")
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                gap(THUMBNAIL_GAP_PX.px)
            }
        }) {
            photos.forEach { photo ->
                CarThumbnail(photo, galleryPhotoUrls, isClickable)
            }
        }

        if (showArrows) {
            ThumbnailScrollArrow(
                label = "▲",
                enabled = canScrollUp,
                alignTop = true,
                onClick = {
                    scrollElement?.scrollBy(0.0, -THUMBNAIL_SCROLL_STEP_PX.toDouble())
                    updateScrollState()
                },
            )
            ThumbnailScrollArrow(
                label = "▼",
                enabled = canScrollDown,
                alignTop = false,
                onClick = {
                    scrollElement?.scrollBy(0.0, THUMBNAIL_SCROLL_STEP_PX.toDouble())
                    updateScrollState()
                },
            )
        }
    }
}

@Composable
private fun ThumbnailScrollArrow(
    label: String,
    enabled: Boolean,
    alignTop: Boolean,
    onClick: () -> Unit,
) {
    Div({
        onClick {
            it.stopPropagation()
            if (enabled) onClick()
        }
        style {
            position(Position.Absolute)
            left(50.percent)
            property("transform", "translateX(-50%)")
            if (alignTop) {
                top(8.px)
            } else {
                bottom(8.px)
            }
            property("z-index", "2")
            width(32.px)
            height(32.px)
            borderRadius(50.percent)
            backgroundColor(rgba(0, 0, 0, 0.5))
            color(CSSColors.White)
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.Center)
            fontSize(16.px)
            cursor(if (enabled) "pointer" else "default")
            property("user-select", "none")
            if (!enabled) {
                opacity(0.35)
            }
        }
    }) {
        Text(label)
    }
}

@Composable
private fun CarThumbnail(
    photo: CarPhotoItem,
    galleryPhotoUrls: List<String>,
    isClickable: Boolean,
) {
    Div({
        style {
            position(Position.Relative)
            flexShrink(0)
            if (isClickable) cursor("pointer")
        }
        if (isClickable) {
            onClick { window.location.href = buildCarPhotosGalleryUrl(galleryPhotoUrls) }
        }
    }) {
        Img(
            src = photo.previewUrl(),
            attrs = {
                style {
                    width(100.percent)
                    height(THUMBNAIL_HEIGHT_PX.px)
                    property("object-fit", "cover")
                    borderRadius(8.px)
                    display(DisplayStyle.Block)
                }
            },
        )
    }
}
