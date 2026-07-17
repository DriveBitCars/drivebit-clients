package my.drivebit.navigation

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import my.drivebit.utils.canonicalizeBrandSearchPath

@Serializable
data class SeoContentTable(
    val headers: List<String>,
    val rows: List<List<String>>,
)

@Serializable
data class SeoContentSection(
    val heading: String,
    val paragraphs: List<String> = emptyList(),
    val bullets: List<String> = emptyList(),
    val table: SeoContentTable? = null,
)

@Serializable
data class SeoLandingBlock(
    val ariaLabel: String,
    val h2: String,
    val paragraphs: List<String>,
    val title: String? = null,
    val description: String? = null,
    val h1: String? = null,
    val sections: List<SeoContentSection>? = null,
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

    fun blockForPath(path: String): SeoLandingBlock? {
        val normalized = normalizePath(path)
        val canonical = canonicalizeBrandSearchPath(normalized) ?: normalized
        return blocksByPath()[canonical] ?: blocksByPath()[normalized]
    }

    fun renderShell(block: SeoLandingBlock): String =
        """    <div class="drivebit-seo-shell">
        <section class="drivebit-seo-text" aria-label="${escapeHtml(block.ariaLabel)}">
            ${renderSectionInnerHtml(block)}
        </section>
    </div>

"""

    fun renderSectionInnerHtml(block: SeoLandingBlock): String =
        buildString {
            val sections = block.sections
            if (sections.isNullOrEmpty()) {
                append("<h2>")
                append(escapeHtml(block.h2))
                append("</h2>")
                block.paragraphs.forEach { paragraph ->
                    append("<p>")
                    append(escapeHtml(paragraph))
                    append("</p>")
                }
            } else {
                sections.forEach { section ->
                    append("<h2>")
                    append(escapeHtml(section.heading))
                    append("</h2>")
                    section.paragraphs.forEach { paragraph ->
                        append("<p>")
                        append(escapeHtml(paragraph))
                        append("</p>")
                    }
                    if (section.bullets.isNotEmpty()) {
                        append("<ul>")
                        section.bullets.forEach { bullet ->
                            append("<li>")
                            append(escapeHtml(bullet))
                            append("</li>")
                        }
                        append("</ul>")
                    }
                    section.table?.let { table ->
                        append("<table>")
                        append("<thead><tr>")
                        table.headers.forEach { header ->
                            append("<th>")
                            append(escapeHtml(header))
                            append("</th>")
                        }
                        append("</tr></thead><tbody>")
                        table.rows.forEach { row ->
                            append("<tr>")
                            row.forEach { cell ->
                                append("<td>")
                                append(escapeHtml(cell))
                                append("</td>")
                            }
                            append("</tr>")
                        }
                        append("</tbody></table>")
                    }
                }
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
