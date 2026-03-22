package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import my.drivebit.viewmodels.CarReviewUi
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexWrap
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.opacity
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun CarReviewsSection(
    reviews: List<CarReviewUi>,
    loading: Boolean,
    error: String?,
    page: Int,
    totalPages: Int,
    totalCount: Int,
    onLoadPage: (Int) -> Unit,
) {
    Column(
        gap = 12.px,
        modifier = {
            padding(16.px)
            borderRadius(12.px)
            border(1.px, LineStyle.Solid, CSSColors.Gray300)
        },
    ) {
        Span({
            style {
                fontSize(16.px)
                fontWeight("600")
                color(CSSColors.Black)
            }
        }) {
            Text("Отзывы")
        }

        when {
            loading && reviews.isEmpty() -> {
                Loader()
            }

            error != null && reviews.isEmpty() -> {
                TextError(error)
            }

            reviews.isEmpty() && !loading && error == null -> {
                Span({
                    style {
                        fontSize(14.px)
                        color(CSSColors.Gray600)
                    }
                }) {
                    Text("Пока нет отзывов об этом автомобиле")
                }
            }
        }

        error?.takeIf { reviews.isNotEmpty() }?.let { message ->
            Span({
                style {
                    fontSize(13.px)
                    color(CSSColors.Gray600)
                }
            }) {
                Text(message)
            }
        }

        if (reviews.isNotEmpty()) {
            Column(gap = 12.px) {
                reviews.forEach { review ->
                    ReviewCard(review)
                }
            }
        }

        if (loading && reviews.isNotEmpty()) {
            Span({
                style {
                    fontSize(13.px)
                    color(CSSColors.Gray600)
                }
            }) {
                Text("Загрузка…")
            }
        }

        if (totalPages > 1) {
            Row(
                gap = 12.px,
                flexWrap = FlexWrap.Wrap,
                alignItems = AlignItems.Center,
                modifier = {
                    marginTop(8.px)
                },
            ) {
                Span({
                    style {
                        fontSize(14.px)
                        color(CSSColors.Gray600)
                    }
                }) {
                    Text("Страница $page из $totalPages · Всего: $totalCount")
                }
                Row(
                    gap = 8.px,
                    alignItems = AlignItems.Center,
                ) {
                    Button({
                        onClick { onLoadPage(page - 1) }
                        if (page <= 1 || loading) {
                            disabled()
                        }
                        style {
                            padding(8.px, 14.px)
                            borderRadius(8.px)
                            border(1.px, LineStyle.Solid, CSSColors.Gray300)
                            backgroundColor(CSSColors.White)
                            color(CSSColors.Black)
                            cursor("pointer")
                            fontSize(14.px)
                            if (page <= 1 || loading) {
                                opacity(0.5)
                            }
                        }
                    }) {
                        Text("Назад")
                    }
                    Button({
                        onClick { onLoadPage(page + 1) }
                        if (page >= totalPages || loading) {
                            disabled()
                        }
                        style {
                            padding(8.px, 14.px)
                            borderRadius(8.px)
                            border(1.px, LineStyle.Solid, CSSColors.Gray300)
                            backgroundColor(CSSColors.White)
                            color(CSSColors.Black)
                            cursor("pointer")
                            fontSize(14.px)
                            if (page >= totalPages || loading) {
                                opacity(0.5)
                            }
                        }
                    }) {
                        Text("Вперёд")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(review: CarReviewUi) {
    Div({
        style {
            padding(12.px)
            borderRadius(8.px)
            border(1.px, LineStyle.Solid, CSSColors.Gray300)
            backgroundColor(CSSColors.White)
        }
    }) {
        Row(
            gap = 8.px,
            alignItems = AlignItems.Center,
        ) {
            Span({
                style {
                    fontSize(14.px)
                    fontWeight("600")
                    color(CSSColors.Black)
                }
            }) {
                Text(review.authorDisplayName)
            }
            review.createdAtDisplay?.let { date ->
                Span({
                    style {
                        fontSize(13.px)
                        color(CSSColors.Gray600)
                    }
                }) {
                    Text("· $date")
                }
            }
        }
        Row(
            gap = 4.px,
            alignItems = AlignItems.Center,
            modifier = {
                marginTop(6.px)
            },
        ) {
            repeat(5) { index ->
                val filled = index < review.stars.coerceIn(0, 5)
                Span({
                    style {
                        fontSize(16.px)
                        color(if (filled) CSSColors.Black else CSSColors.Gray300)
                    }
                }) {
                    Text("★")
                }
            }
        }
        review.text?.takeIf { it.isNotBlank() }?.let { body ->
            Span({
                style {
                    display(DisplayStyle.Block)
                    marginTop(8.px)
                    fontSize(14.px)
                    color(CSSColors.Black)
                    property("white-space", "pre-wrap")
                }
            }) {
                Text(body)
            }
        }
    }
}
