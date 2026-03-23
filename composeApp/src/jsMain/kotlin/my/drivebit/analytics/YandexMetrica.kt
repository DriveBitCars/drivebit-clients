package my.drivebit.analytics

import kotlin.js.jsTypeOf
import kotlinx.browser.window

private const val YANDEX_COUNTER_ID = 105947907
private const val ARENDA_GOAL = "arenda"

fun reachYandexGoalArenda() {
    val ym = window.asDynamic().ym
    if (jsTypeOf(ym) == "function") {
        ym(YANDEX_COUNTER_ID, "reachGoal", ARENDA_GOAL)
    }
}
