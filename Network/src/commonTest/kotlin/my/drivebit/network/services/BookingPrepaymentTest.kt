package my.drivebit.network.services

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BookingPrepaymentTest {
    @Test
    fun renterFullOrBalancePaymentLabel_changesAfterPrepaymentPaid() {
        val before =
            booking(
                prepaymentPaidAt = null,
                totalAmountWithDeposit = 12000.0,
            )
        val after =
            booking(
                prepaymentPaidAt = "2026-05-28T12:00:00Z",
                balanceDueAmount = 9000.0,
            )

        assertEquals("Оплатить полностью", before.renterFullOrBalancePaymentLabel())
        assertEquals("Оплатить остаток", after.renterFullOrBalancePaymentLabel())
    }

    @Test
    fun renterFullOrBalanceAmountRub_usesBalanceDueAfterPrepayment() {
        val booking =
            booking(
                prepaymentPaidAt = "2026-05-28T12:00:00Z",
                balanceDueAmount = 9000.4,
                totalAmountWithDeposit = 12000.0,
            )

        assertEquals(9000, booking.renterFullOrBalanceAmountRub())
    }

    @Test
    fun prepaymentButtonLabel_showsAmountFromApi() {
        val booking =
            booking(
                prepaymentPercent = 30.0,
                prepaymentAmount = 3000.0,
                canPayPrepayment = true,
            )

        assertEquals("Предоплата (3000 ₽)", booking.prepaymentButtonLabel())
    }

    @Test
    fun canPayPrepayment_falseWhenBackendSaysUnavailable() {
        val booking =
            booking(
                prepaymentPercent = 0.0,
                prepaymentAmount = 0.0,
                canPayPrepayment = false,
                status = "Confirmed",
            )

        assertFalse(booking.canPayPrepayment)
    }

    @Test
    fun statusAllowsRenterPayment_onlyForConfirmed() {
        assertTrue(booking(status = "Confirmed").statusAllowsRenterPayment())
        assertFalse(booking(status = "Paid").statusAllowsRenterPayment())
    }

    private fun booking(
        status: String = "Confirmed",
        prepaymentPaidAt: String? = null,
        prepaymentPercent: Double = 0.0,
        prepaymentAmount: Double = 0.0,
        canPayPrepayment: Boolean = false,
        totalAmountWithDeposit: Double = 0.0,
        balanceDueAmount: Double = 0.0,
    ): BookingDTO =
        BookingDTO(
            id = "550e8400-e29b-41d4-a716-446655440000",
            carId = "660e8400-e29b-41d4-a716-446655440001",
            renterId = "770e8400-e29b-41d4-a716-446655440002",
            ownerId = "880e8400-e29b-41d4-a716-446655440003",
            startAt = "2026-05-28T10:00:00Z",
            endAt = "2026-05-30T10:00:00Z",
            totalAmount = 10000.0,
            deposit = 2000.0,
            totalAmountWithDeposit = totalAmountWithDeposit,
            prepaymentPercent = prepaymentPercent,
            prepaymentAmount = prepaymentAmount,
            balanceDueAmount = balanceDueAmount,
            prepaymentPaidAt = prepaymentPaidAt,
            canPayPrepayment = canPayPrepayment,
            status = status,
            createdAt = "2026-05-27T10:00:00Z",
        )
}
