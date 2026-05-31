package my.drivebit.navigation

import kotlinx.browser.document
import org.w3c.dom.Element

object SeoBlocks {
    fun blockForPath(path: String): SeoLandingBlock? = SeoLandingBlocks.blockForPath(path)

    fun updateForPath(path: String) {
        val shell = document.querySelector(".drivebit-seo-shell") as? Element ?: return
        val block = blockForPath(path)
        if (block == null) {
            shell.innerHTML = ""
            shell.setAttribute("hidden", "")
            return
        }
        shell.removeAttribute("hidden")
        val section = shell.querySelector(".drivebit-seo-text") as? Element
        if (section == null) {
            shell.innerHTML =
                """<section class="drivebit-seo-text" aria-label="${SeoLandingBlocks.escapeHtml(block.ariaLabel)}">${
                    SeoLandingBlocks.renderSectionInnerHtml(block)
                }</section>"""
            return
        }
        section.setAttribute("aria-label", block.ariaLabel)
        section.innerHTML = SeoLandingBlocks.renderSectionInnerHtml(block)
    }
}
