package my.drivebit.components

const val CAR_PHOTO_SWIPE_THRESHOLD_PX = 40.0

fun isCarPhotoSwipeSignificant(
    startX: Double,
    endX: Double,
    thresholdPx: Double = CAR_PHOTO_SWIPE_THRESHOLD_PX,
): Boolean = kotlin.math.abs(startX - endX) > thresholdPx

fun resolveCarPhotoSwipeIndex(
    currentIndex: Int,
    photoCount: Int,
    startX: Double,
    endX: Double,
    thresholdPx: Double = CAR_PHOTO_SWIPE_THRESHOLD_PX,
): Int {
    if (photoCount <= 1) return 0
    val safeIndex = currentIndex.coerceIn(0, photoCount - 1)
    if (!isCarPhotoSwipeSignificant(startX, endX, thresholdPx)) {
        return safeIndex
    }
    val swipeDelta = startX - endX
    return when {
        swipeDelta > thresholdPx && safeIndex < photoCount - 1 -> safeIndex + 1
        swipeDelta < -thresholdPx && safeIndex > 0 -> safeIndex - 1
        else -> safeIndex
    }
}
