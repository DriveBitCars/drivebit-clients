package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.minWidth
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun PaginationBar(
    currentPage: Int,
    totalPages: Int,
    totalCount: Int,
    pageSize: Int,
    currentPageItemCount: Int? = null,
    onPageChange: (Int) -> Unit,
) {
    val hasPrev = currentPage > 0
    val hasNext = currentPage < totalPages - 1
    val pageButtons = visiblePageNumbers(currentPage, totalPages)

    Row(
        gap = 16.px,
        alignItems = AlignItems.Center,
        justifyContent = JustifyContent.Center,
        modifier = {
            padding(24.px, 0.px)
            property("flex-wrap", "wrap")
        },
        attrs = {
            classes("drivebit-pagination-bar")
        },
    ) {
        if (totalPages > 1) {
            Row(
                gap = 8.px,
                alignItems = AlignItems.Center,
                modifier = {
                    property("flex-wrap", "wrap")
                    justifyContent(JustifyContent.Center)
                },
            ) {
                PaginationChip(
                    label = "← Назад",
                    enabled = hasPrev,
                    active = false,
                    onClick = { if (hasPrev) onPageChange(currentPage - 1) },
                )
                pageButtons.forEach { page ->
                    PaginationChip(
                        label = (page + 1).toString(),
                        enabled = true,
                        active = page == currentPage,
                        onClick = { onPageChange(page) },
                    )
                }
                PaginationChip(
                    label = "Вперёд →",
                    enabled = hasNext,
                    active = false,
                    onClick = { if (hasNext) onPageChange(currentPage + 1) },
                )
            }
        }
        Span({
            style {
                applyTypography(CSSTypography.Styles.body)
                fontSize(CSSTypography.FontSize.sm)
                color(CSSColors.Gray600)
            }
        }) {
            Text(paginationSummary(currentPage, totalCount, pageSize, currentPageItemCount))
        }
    }
}

@Composable
private fun PaginationChip(
    label: String,
    enabled: Boolean,
    active: Boolean,
    onClick: () -> Unit,
) {
    Div({
        style {
            height(40.px)
            minWidth(40.px)
            padding(0.px, 14.px)
            borderRadius(10.px)
            property("display", "inline-flex")
            property("align-items", "center")
            property("justify-content", "center")
            property("box-sizing", "border-box")
            if (active) {
                backgroundColor(CSSColors.Blue)
                border(1.px, LineStyle.Solid, CSSColors.Blue)
                property("box-shadow", "none")
            } else {
                backgroundColor(CSSColors.White)
                border(1.px, LineStyle.Solid, Color("#d8dde6"))
            }
            cursor(if (enabled) "pointer" else "not-allowed")
            property("opacity", if (enabled || active) "1" else "0.45")
            property("transition", "background-color 0.15s ease, border-color 0.15s ease, color 0.15s ease")
            property("user-select", "none")
        }
        classes("drivebit-pagination-chip")
        if (active) {
            classes("is-active")
        }
        onClick {
            if (enabled) onClick()
        }
    }) {
        Span({
            style {
                applyTypography(CSSTypography.Styles.body)
                fontSize(14.px)
                fontWeight(CSSTypography.FontWeight.medium)
                property("line-height", "1")
                color(if (active) CSSColors.White else if (enabled) Color("#152032") else CSSColors.Gray600)
            }
        }) {
            Text(label)
        }
    }
}

internal fun visiblePageNumbers(
    currentPage: Int,
    totalPages: Int,
    maxButtons: Int = 5,
): List<Int> {
    if (totalPages <= 0) return emptyList()
    if (totalPages <= maxButtons) return (0 until totalPages).toList()
    val half = maxButtons / 2
    var start = (currentPage - half).coerceAtLeast(0)
    var end = start + maxButtons
    if (end > totalPages) {
        end = totalPages
        start = (end - maxButtons).coerceAtLeast(0)
    }
    return (start until end).toList()
}

fun paginationSummary(
    currentPage: Int,
    totalCount: Int,
    pageSize: Int,
    currentPageItemCount: Int? = null,
): String {
    if (totalCount <= 0) return "Показано 0 из 0"
    val from = currentPage * pageSize + 1
    if (from > totalCount) return "Показано 0 из $totalCount"
    if (currentPageItemCount != null && currentPageItemCount <= 0) return "Показано 0 из $totalCount"
    val to =
        currentPageItemCount
            ?.let { minOf(from + it - 1, totalCount) }
            ?: minOf((currentPage + 1) * pageSize, totalCount)
    return "Показано $from–$to из $totalCount"
}
