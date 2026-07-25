package my.drivebit.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class PaymentFunnelAnalyticsTest {
    @Test
    fun goals_haveStableIdentifiersForMetrika() {
        assertEquals("pay_click", PaymentFunnelGoals.CLICK)
        assertEquals("pay_redirect", PaymentFunnelGoals.REDIRECT)
        assertEquals("pay_fail", PaymentFunnelGoals.FAIL)
        assertEquals("pay_already_paid", PaymentFunnelGoals.ALREADY_PAID)
        assertEquals("payment_success", PaymentFunnelGoals.SUCCESS_PAGE)
        assertEquals("payment_failure", PaymentFunnelGoals.FAILURE_PAGE)
    }

    @Test
    fun clickParams_includeSourceKindAndBookingId() {
        val params =
            paymentFunnelParams(
                source = PaymentFunnelSource.Chat,
                kind = PaymentFunnelKind.Prepay,
                bookingId = "87306748-e188-47a7-b35f-908d2b5ec224",
            )
        assertEquals("chat", params["source"])
        assertEquals("prepay", params["kind"])
        assertEquals("87306748-e188-47a7-b35f-908d2b5ec224", params["bookingId"])
    }

    @Test
    fun failParams_truncateLongMessage() {
        val long = "x".repeat(300)
        val params =
            paymentFunnelParams(
                source = PaymentFunnelSource.PaymentLink,
                kind = PaymentFunnelKind.Full,
                bookingId = "b1",
                message = long,
            )
        assertEquals(120, params.getValue("message").length)
    }
}
