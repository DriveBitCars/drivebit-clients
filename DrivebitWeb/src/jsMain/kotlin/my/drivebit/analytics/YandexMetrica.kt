package my.drivebit.analytics

import kotlinx.browser.document
import kotlinx.browser.window
import my.drivebit.utils.BookingFunnelGoals
import my.drivebit.utils.BookingFunnelSource
import my.drivebit.utils.PaymentFunnelGoals
import my.drivebit.utils.PaymentFunnelKind
import my.drivebit.utils.PaymentFunnelSource
import my.drivebit.utils.bookingFunnelCreateParams
import my.drivebit.utils.bookingFunnelIntentParams
import my.drivebit.utils.paymentFunnelParams
import org.w3c.dom.HTMLScriptElement
import kotlin.js.js
import kotlin.js.jsTypeOf

private const val YANDEX_COUNTER_ID = 105947907
private const val ARENDA_GOAL = "arenda"
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
    script.defer = true
    document.head?.appendChild(script)
}

private fun reachYandexGoal(
    goal: String,
    params: Map<String, String> = emptyMap(),
) {
    ensureYandexMetrikaStub()
    if (params.isEmpty()) {
        window.asDynamic().ym(YANDEX_COUNTER_ID, "reachGoal", goal)
        return
    }
    val payload = js("{}")
    params.forEach { (key, value) ->
        payload[key] = value
    }
    window.asDynamic().ym(YANDEX_COUNTER_ID, "reachGoal", goal, payload)
}

fun reachYandexGoalArenda() {
    reachYandexGoal(ARENDA_GOAL)
}

fun reachYandexGoalBron() {
    reachYandexGoal(BookingFunnelGoals.LEGACY)
}

fun reachYandexGoalBronClick(carId: String) {
    reachYandexGoal(BookingFunnelGoals.LEGACY)
    reachYandexGoal(
        BookingFunnelGoals.CLICK,
        bookingFunnelIntentParams(BookingFunnelSource.Click, carId),
    )
}

fun reachYandexGoalBronAuto(carId: String) {
    reachYandexGoal(BookingFunnelGoals.LEGACY)
    reachYandexGoal(
        BookingFunnelGoals.AUTO,
        bookingFunnelIntentParams(BookingFunnelSource.Auto, carId),
    )
}

fun reachYandexGoalBronCreateOk(
    source: BookingFunnelSource,
    carId: String,
    bookingId: String,
) {
    reachYandexGoal(
        BookingFunnelGoals.CREATE_OK,
        bookingFunnelCreateParams(source, carId, bookingId),
    )
}

fun reachYandexGoalBronCreateFail(
    source: BookingFunnelSource,
    carId: String,
    message: String?,
) {
    reachYandexGoal(
        BookingFunnelGoals.CREATE_FAIL,
        bookingFunnelCreateParams(source, carId, bookingId = null, message = message),
    )
}

fun reachYandexGoalPayClick(
    source: PaymentFunnelSource,
    kind: PaymentFunnelKind,
    bookingId: String,
) {
    reachYandexGoal(
        PaymentFunnelGoals.CLICK,
        paymentFunnelParams(source, kind, bookingId),
    )
}

fun reachYandexGoalPayRedirect(
    source: PaymentFunnelSource,
    kind: PaymentFunnelKind,
    bookingId: String,
) {
    reachYandexGoal(
        PaymentFunnelGoals.REDIRECT,
        paymentFunnelParams(source, kind, bookingId),
    )
}

fun reachYandexGoalPayFail(
    source: PaymentFunnelSource,
    kind: PaymentFunnelKind,
    bookingId: String,
    message: String,
) {
    reachYandexGoal(
        PaymentFunnelGoals.FAIL,
        paymentFunnelParams(source, kind, bookingId, message),
    )
}

fun reachYandexGoalPayAlreadyPaid(
    source: PaymentFunnelSource,
    kind: PaymentFunnelKind,
    bookingId: String,
) {
    reachYandexGoal(
        PaymentFunnelGoals.ALREADY_PAID,
        paymentFunnelParams(source, kind, bookingId),
    )
}

fun reachYandexGoalPaymentSuccessPage() {
    reachYandexGoal(PaymentFunnelGoals.SUCCESS_PAGE)
}

fun reachYandexGoalPaymentFailurePage() {
    reachYandexGoal(PaymentFunnelGoals.FAILURE_PAGE)
}
