package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.padding
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
                            fontSize(15.px)
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
                            color(CSSColors.Gray600)
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
        UserAvatar(
            src = sanitizedUrl,
            size = 72.px,
            alt = name,
        )
        return
    }

    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Div({
        style {
            width(72.px)
            height(72.px)
            borderRadius(50.percent)
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.Center)
            backgroundColor(CSSColors.Gray300)
            color(CSSColors.Gray600)
            fontSize(20.px)
            fontWeight("700")
        }
    }) {
        Text(initial)
    }
}

@Composable
private fun OwnerStatChip(text: String) {
    Row(
        gap = 6.px,
        alignItems = AlignItems.Center,
        modifier = {
            padding(8.px)
            borderRadius(10.px)
            border(1.px, LineStyle.Solid, CSSColors.Gray300)
            backgroundColor(CSSColors.White)
            color(CSSColors.Gray600)
            fontSize(14.px)
        },
    ) {
        Text(text)
    }
}

private fun Double.toRatingString(): String = (round(this * 10) / 10.0).toString()
