package my.drivebit.shell

import kotlinx.browser.document

fun hasStaticHtmlHeaderShell(): Boolean =
    document.getElementById("drivebit-header-compose") != null

fun hasExternalAppHeaderMount(): Boolean = hasStaticHtmlHeaderShell()
