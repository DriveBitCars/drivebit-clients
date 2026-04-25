package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import my.drivebit.design.CSSColors
import my.drivebit.repositories.ParticipantAvatarCache
import my.drivebit.utils.extractPathFromApiUrl
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

@Composable
fun ParticipantAvatar(
    userId: String,
    name: String,
    initialAvatarUrl: String? = null,
    size: CSSLengthOrPercentageValue = 48.px,
) {
    val cache: ParticipantAvatarCache = koinInject()
    val avatarUrlByUserId by cache.avatarUrlByUserId.collectAsState()

    LaunchedEffect(userId) {
        cache.fetchIfNeeded(userId, initialAvatarUrl)
    }

    val rawAvatarUrl =
        initialAvatarUrl?.takeIf { it.isNotBlank() }
            ?: avatarUrlByUserId[userId]?.takeIf { it.isNotBlank() }
    val avatarUrl =
        rawAvatarUrl?.let { raw ->
            if (raw.contains("155.212.170.94")) extractPathFromApiUrl(raw) else raw
        }

    if (avatarUrl != null) {
        Div({
            style {
                minWidth(size)
                flexShrink(0)
            }
        }) {
            UserAvatar(
                src = avatarUrl,
                size = size,
                alt = name,
            )
        }
        return
    }

    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Div({
        style {
            width(size)
            height(size)
            minWidth(size)
            flexShrink(0)
            borderRadius(50.percent)
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            alignItems(AlignItems.Center)
            property("justify-content", "center")
            backgroundColor(CSSColors.Gray300)
            color(CSSColors.Gray600)
            fontSize(18.px)
            fontWeight("700")
        }
    }) {
        Text(initial)
    }
}
