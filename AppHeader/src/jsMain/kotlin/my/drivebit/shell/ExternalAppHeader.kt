package my.drivebit.shell

import kotlinx.browser.document

fun hasExternalAppHeaderMount(): Boolean =
    document.getElementById("drivebit-app-header") != null
