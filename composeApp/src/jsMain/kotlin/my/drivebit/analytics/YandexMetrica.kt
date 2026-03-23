package my.drivebit.analytics

import kotlin.js.jsTypeOf
import kotlinx.browser.window

private const val YANDEX_COUNTER_ID = 105947907
private const val ARENDA_GOAL = "arenda"
private const val BRON_GOAL = "bron"

private fun reachYandexGoal(goal: String) {
    val ym = window.asDynamic().ym
    if (jsTypeOf(ym) == "function") {
        ym(YANDEX_COUNTER_ID, "reachGoal", goal)
    }
}

fun reachYandexGoalArenda() {
    reachYandexGoal(ARENDA_GOAL)
}

fun reachYandexGoalBron() {
    reachYandexGoal(BRON_GOAL)
}
