package my.drivebit.components

import androidx.compose.runtime.Composable
import kotlinx.browser.window
import my.drivebit.design.CSSColors
import my.drivebit.network.services.AWAITING_OWNER_CONFIRMATION_PHRASE
import my.drivebit.network.services.contractDownloadPagePath
import my.drivebit.network.services.extractBookingIdFromContractDownloadUrl
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Text

private const val CONTRACT_LINK_LABEL = "Скачать договор аренды"
private const val CONTRACT_LINK_PREFIX = "$CONTRACT_LINK_LABEL: "

@Composable
fun MessageTextWithDealsLink(
    text: String,
    stopPropagation: Boolean = false,
) {
    if (text.isBlank()) return

    val contractBookingId = extractBookingIdFromContractDownloadUrl(text)
    when {
        text.contains(AWAITING_OWNER_CONFIRMATION_PHRASE) -> {
            val before = text.substringBefore(AWAITING_OWNER_CONFIRMATION_PHRASE)
            val after = text.substringAfter(AWAITING_OWNER_CONFIRMATION_PHRASE)

            if (before.isNotBlank()) {
                MessageTextWithDealsLink(before.trimEnd(), stopPropagation)
            }
            InlineLink(
                href = "/my-deals",
                label = AWAITING_OWNER_CONFIRMATION_PHRASE,
                stopPropagation = stopPropagation,
            )
            if (after.isNotBlank()) {
                MessageTextWithDealsLink(after.trimStart(), stopPropagation)
            }
        }
        contractBookingId != null && text.contains(CONTRACT_LINK_PREFIX) -> {
            val before = text.substringBefore(CONTRACT_LINK_PREFIX)
            if (before.isNotBlank()) {
                Text(before.trimEnd())
            }
            InlineLink(
                href = contractDownloadPagePath(contractBookingId),
                label = CONTRACT_LINK_LABEL,
                stopPropagation = stopPropagation,
            )
        }
        contractBookingId != null -> {
            val match = contractDownloadUrlRegex.find(text)
            if (match != null) {
                val before = text.substring(0, match.range.first)
                val after = text.substring(match.range.last + 1)
                if (before.isNotBlank()) {
                    Text(before)
                }
                InlineLink(
                    href = contractDownloadPagePath(contractBookingId),
                    label = CONTRACT_LINK_LABEL,
                    stopPropagation = stopPropagation,
                )
                if (after.isNotBlank()) {
                    Text(after)
                }
            } else {
                InlineLink(
                    href = contractDownloadPagePath(contractBookingId),
                    label = CONTRACT_LINK_LABEL,
                    stopPropagation = stopPropagation,
                )
            }
        }
        else -> Text(text)
    }
}

private val contractDownloadUrlRegex =
    Regex("""(?:https?://(?:www\.)?drivebit\.ru)?/download-booking-contract\?bookingId=[0-9a-fA-F-]{36}""")

@Composable
private fun InlineLink(
    href: String,
    label: String,
    stopPropagation: Boolean,
) {
    A(attrs = {
        attr("href", href)
        onClick { event ->
            event.preventDefault()
            if (stopPropagation) event.stopPropagation()
            window.location.href = href
        }
        style {
            color(CSSColors.Blue)
            property("text-decoration", "underline")
            cursor("pointer")
        }
    }) {
        Text(label)
    }
}
