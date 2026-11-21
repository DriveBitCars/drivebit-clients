package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import my.drivebit.viewmodels.CarReviewUi
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexWrap
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.marginTop
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
        attrs = { classes("db-car-section") },
    ) {
        Span({
            classes("db-car-section__title")
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
                        color(CSSColors.Gray4)
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
                    color(CSSColors.Gray4)
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
                    color(CSSColors.Gray4)
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
                        color(CSSColors.Gray4)
                    }
                }) {
                    Text("Страница $page из $totalPages · Всего: $totalCount")
                }
                Row(
                    gap = 8.px,
                    alignItems = AlignItems.Center,
                ) {
                    Button({
                        classes("db-car-pager-btn")
                        onClick { onLoadPage(page - 1) }
                        if (page <= 1 || loading) {
                            disabled()
                        }
                    }) {
                        Text("Назад")
                    }
                    Button({
                        classes("db-car-pager-btn")
                        onClick { onLoadPage(page + 1) }
                        if (page >= totalPages || loading) {
                            disabled()
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
        classes("db-car-review")
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
                        color(CSSColors.Gray4)
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
            attrs = { classes("db-car-review__stars") },
        ) {
            repeat(5) { index ->
                val filled = index < review.stars.coerceIn(0, 5)
                Span({
                    style {
                        fontSize(16.px)
                        color(if (filled) CSSColors.Blue else CSSColors.Gray6)
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
                    property("line-height", "1.5")
                }
            }) {
                Text(body)
            }
        }
    }
}
