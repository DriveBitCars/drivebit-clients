package my.drivebit.navigation

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SeoLandingBlock(
    val ariaLabel: String,
    val h2: String,
    val paragraphs: List<String>,
)

object SeoLandingBlocks {
    const val RESOURCE_PATH = "/seo/landing-blocks.json"

    private val json = Json { ignoreUnknownKeys = true }

    private var cached: Map<String, SeoLandingBlock>? = null

    fun blocksByPath(): Map<String, SeoLandingBlock> {
        cached?.let { return it }
        val loaded = loadFromResource()
        cached = loaded
        return loaded
    }

    fun blockForPath(path: String): SeoLandingBlock? = blocksByPath()[normalizePath(path)]

    fun renderShell(block: SeoLandingBlock): String {
        val paragraphsHtml =
            block.paragraphs.joinToString("") { paragraph ->
                "<p>${escapeHtml(paragraph)}</p>"
            }
        return """    <div class="drivebit-seo-shell">
        <section class="drivebit-seo-text" aria-label="${escapeHtml(block.ariaLabel)}">
            <h2>${escapeHtml(block.h2)}</h2>
            $paragraphsHtml
        </section>
    </div>

"""
    }

    fun renderSectionInnerHtml(block: SeoLandingBlock): String =
        buildString {
            append("<h2>")
            append(escapeHtml(block.h2))
            append("</h2>")
            block.paragraphs.forEach { paragraph ->
                append("<p>")
                append(escapeHtml(paragraph))
                append("</p>")
            }
        }

    fun escapeHtml(text: String): String =
        text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")

    fun normalizePath(path: String): String {
        val withoutQuery = path.substringBefore('?').substringBefore('#')
        if (withoutQuery.isBlank()) return "/"
        val normalized = if (withoutQuery.startsWith("/")) withoutQuery else "/$withoutQuery"
        return normalized.trimEnd('/').ifEmpty { "/" }
    }

    internal fun parseBlocksJson(raw: String): Map<String, SeoLandingBlock> =
        json.decodeFromString<Map<String, SeoLandingBlock>>(raw)

    private fun loadFromResource(): Map<String, SeoLandingBlock> {
        val raw = readResourceText(RESOURCE_PATH)
        return parseBlocksJson(raw)
    }

    private fun readResourceText(url: String): String = readResourceTextBlocking(url)

    internal fun readResourceTextBlocking(url: String): String =
        js(
            """
            (function(url) {
                var xhr = new XMLHttpRequest();
                xhr.open('GET', url, false);
                xhr.send(null);
                if (xhr.status !== 200) {
                    throw new Error('Failed to load ' + url + ': HTTP ' + xhr.status);
                }
                return xhr.responseText;
            })
            """,
        ).unsafeCast<(String) -> String>()(url)
}
