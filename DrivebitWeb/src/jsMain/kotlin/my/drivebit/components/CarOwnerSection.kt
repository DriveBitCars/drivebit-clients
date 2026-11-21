package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.flexShrink
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.minWidth
import org.jetbrains.compose.web.css.overflow
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import kotlin.math.round

@Composable
fun CarOwnerSection(
    name: String,
    avatarUrl: String?,
    memberSince: String?,
    rating: Double?,
    tripsCount: Int?,
    verificationLabels: List<String> = emptyList(),
) {
    val ownerName = name.takeIf { it.isNotBlank() } ?: "Владелец"

    Column(
        gap = 14.px,
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
            Text("Владелец")
        }

        Row(
            gap = 12.px,
            alignItems = AlignItems.Center,
        ) {
            OwnerAvatar(
                name = ownerName,
                avatarUrl = avatarUrl,
            )

            Column(gap = 6.px) {
                Row(
                    gap = 8.px,
                    alignItems = AlignItems.Center,
                ) {
                    Span({
                        style {
                            fontSize(16.px)
                            fontWeight("600")
                            color(CSSColors.Black)
                        }
                    }) {
                        Text(ownerName)
                    }
                    VerificationBadgeRow(verificationLabels)
                }

                memberSince?.takeIf { it.isNotBlank() }?.let { since ->
                    Span({
                        style {
                            fontSize(14.px)
                            color(CSSColors.Gray4)
                        }
                    }) {
                        Text("С нами с $since")
                    }
                }
            }
        }

        Row(
            gap = 8.px,
            alignItems = AlignItems.Center,
        ) {
            rating?.let { value ->
                OwnerStatChip(
                    text = "Рейтинг ${value.toRatingString()}",
                )
            }

            tripsCount?.let { trips ->
                OwnerStatChip(
                    text = "$trips поездок",
                )
            }

            memberSince?.takeIf { it.isNotBlank() }?.let { since ->
                OwnerStatChip(
                    text = "Присоединился $since",
                )
            }
        }
    }
}

@Composable
private fun OwnerAvatar(
    name: String,
    avatarUrl: String?,
) {
    val sanitizedUrl = avatarUrl?.takeIf { it.isNotBlank() }

    if (sanitizedUrl != null) {
        Div({
            style {
                width(64.px)
                height(64.px)
                minWidth(64.px)
                flexShrink(0)
                borderRadius(50.percent)
                overflow("hidden")
            }
        }) {
            UserAvatar(
                src = sanitizedUrl,
                size = 64.px,
                alt = name,
            )
        }
        return
    }

    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Div({
        style {
            width(64.px)
            height(64.px)
            minWidth(64.px)
            flexShrink(0)
            borderRadius(50.percent)
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.Center)
            backgroundColor(CSSColors.Gray7)
            color(CSSColors.Blue)
            fontSize(20.px)
            fontWeight("700")
        }
    }) {
        Text(initial)
    }
}

@Composable
private fun OwnerStatChip(text: String) {
    Span({
        classes("db-car-chip")
    }) {
        Text(text)
    }
}

private fun Double.toRatingString(): String = (round(this * 10) / 10.0).toString()
