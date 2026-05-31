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
    onPageChange: (Int) -> Unit,
) {
    if (totalPages <= 1 || totalCount == 0) return

    val from = currentPage * pageSize + 1
    val to = minOf((currentPage + 1) * pageSize, totalCount)
    val hasPrev = currentPage > 0
    val hasNext = currentPage < totalPages - 1

    Row(
        gap = 16.px,
        alignItems = AlignItems.Center,
        justifyContent = JustifyContent.Center,
        modifier = {
            padding(24.px, 0.px)
        },
    ) {
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
        Span({
            style {
                applyTypography(CSSTypography.Styles.body)
                fontSize(CSSTypography.FontSize.base)
                color(CSSColors.Gray600)
            }
        }) {
            Text("Показано $from–$to из $totalCount")
        }
    }
}
