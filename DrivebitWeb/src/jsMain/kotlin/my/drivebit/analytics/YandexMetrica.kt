package my.drivebit.analytics

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLScriptElement
import kotlin.js.js
import kotlin.js.jsTypeOf

private const val YANDEX_COUNTER_ID = 105947907
private const val ARENDA_GOAL = "arenda"
private const val BRON_GOAL = "bron"
private const val DEFERRED_LOADER_PATH = "/vendor/drivebit-third-party-deferred.js"

private fun ensureYandexMetrikaStub() {
    val w = window.asDynamic()
    if (jsTypeOf(w.ym) == "function") {
        return
    }
    w.ym = js("function(){(window.ym.a=window.ym.a||[]).push(arguments)}")
    if (document.querySelector("script[src*='drivebit-third-party-deferred']") != null) {
        return
    }
    val script = document.createElement("script") as HTMLScriptElement
    script.src = DEFERRED_LOADER_PATH
    script.type = "module"
    document.head?.appendChild(script)
}

private fun reachYandexGoal(goal: String) {
    ensureYandexMetrikaStub()
    window.asDynamic().ym(YANDEX_COUNTER_ID, "reachGoal", goal)
}

fun reachYandexGoalArenda() {
    reachYandexGoal(ARENDA_GOAL)
}

fun reachYandexGoalBron() {
    reachYandexGoal(BRON_GOAL)
}
