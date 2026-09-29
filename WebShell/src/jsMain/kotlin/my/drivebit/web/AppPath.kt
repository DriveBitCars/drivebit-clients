package my.drivebit.web

import kotlinx.browser.window
import my.drivebit.utils.stripWebBasePath

/** Browser pathname with GitHub Pages project base stripped (app routing path). */
fun currentAppPathname(): String =
    stripWebBasePath(window.location.pathname, window.location.hostname)

/** Pathname + search for app routing (base stripped from pathname only). */
fun currentAppLocationHref(): String = currentAppPathname() + window.location.search
