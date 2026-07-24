package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.justifyContent
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

    Row(
        gap = 16.px,
        alignItems = AlignItems.Center,
        justifyContent = JustifyContent.Center,
        modifier = {
            padding(24.px, 0.px)
        },
        attrs = {
            classes("drivebit-pagination-bar")
        },
    ) {
        if (totalPages > 1) {
            Row(
                gap = 8.px,
                alignItems = AlignItems.Center,
            ) {
                Div({
                    style {
                        padding(8.px, 12.px)
                        borderRadius(8.px)
                        border(1.px, LineStyle.Solid, CSSColors.Gray300)
                        cursor(if (hasPrev) "pointer" else "not-allowed")
                        property("opacity", if (hasPrev) "1" else "0.5")
                        property("transition", "all 0.2s ease")
                    }
                    onClick {
                        if (hasPrev) onPageChange(currentPage - 1)
                    }
                }) {
                    Span({
                        style {
                            applyTypography(CSSTypography.Styles.body)
                            fontSize(CSSTypography.FontSize.base)
                            fontWeight(CSSTypography.FontWeight.medium)
                            color(if (hasPrev) CSSColors.Black else CSSColors.Gray600)
                        }
                    }) {
                        Text("Назад")
                    }
                }
                Div({
                    style {
                        padding(8.px, 12.px)
                        borderRadius(8.px)
                        border(1.px, LineStyle.Solid, CSSColors.Gray300)
                        cursor(if (hasNext) "pointer" else "not-allowed")
                        property("opacity", if (hasNext) "1" else "0.5")
                        property("transition", "all 0.2s ease")
                    }
                    onClick {
                        if (hasNext) onPageChange(currentPage + 1)
                    }
                }) {
                    Span({
                        style {
                            applyTypography(CSSTypography.Styles.body)
                            fontSize(CSSTypography.FontSize.base)
                            fontWeight(CSSTypography.FontWeight.medium)
                            color(if (hasNext) CSSColors.Black else CSSColors.Gray600)
                        }
                    }) {
                        Text("Вперёд")
                    }
                }
            }
        }
        Span({
            style {
                applyTypography(CSSTypography.Styles.body)
                fontSize(CSSTypography.FontSize.base)
                color(CSSColors.Gray600)
            }
        }) {
            Text(paginationSummary(currentPage, totalCount, pageSize, currentPageItemCount))
        }
    }
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
