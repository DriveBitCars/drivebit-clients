package my.drivebit.utils

fun urlPageToUiIndex(page: Int): Int = page.coerceAtLeast(1) - 1

fun uiIndexToUrlPage(uiIndex: Int): Int = uiIndex.coerceAtLeast(0) + 1
