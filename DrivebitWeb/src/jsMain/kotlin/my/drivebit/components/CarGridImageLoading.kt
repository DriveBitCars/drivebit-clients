package my.drivebit.components

const val CAR_GRID_EAGER_MOBILE_COUNT = 2
const val CAR_GRID_EAGER_DESKTOP_COUNT = 3

fun carGridImageLoadsEagerly(
    gridIndex: Int,
    isMobileViewport: Boolean,
): Boolean {
    val eagerCount = if (isMobileViewport) CAR_GRID_EAGER_MOBILE_COUNT else CAR_GRID_EAGER_DESKTOP_COUNT
    return gridIndex < eagerCount
}

fun carGridImageLoadingAttr(
    gridIndex: Int,
    isMobileViewport: Boolean,
): String = if (carGridImageLoadsEagerly(gridIndex, isMobileViewport)) "eager" else "lazy"
