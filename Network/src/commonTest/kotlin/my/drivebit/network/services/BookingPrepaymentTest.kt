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
    fun statusAllowsRenterPayment_usesCanPayFullAmountFromApi() {
        assertTrue(
            booking(
                status = "Confirmed",
                canPayFullAmount = true,
            ).statusAllowsRenterPayment(),
        )
        assertTrue(
            booking(
                status = "PrePaid",
                canPayFullAmount = true,
            ).statusAllowsRenterPayment(),
        )
        assertFalse(
            booking(
                status = "Paid",
                canPayFullAmount = false,
            ).statusAllowsRenterPayment(),
        )
    }

    @Test
    fun statusAllowsContractDownload_includesContractSignatureStatuses() {
        assertTrue(booking(status = "Confirmed").statusAllowsContractDownload())
        assertTrue(booking(status = "PrePaid").statusAllowsContractDownload())
        assertTrue(booking(status = "ContractSignedByOwner").statusAllowsContractDownload())
        assertTrue(booking(status = "ContractSignedByBoth").statusAllowsContractDownload())
        assertFalse(booking(status = "Pending").statusAllowsContractDownload())
    }

    @Test
    fun statusAllowsInspectionAct_requiresPaymentOrActiveFlow() {
        assertFalse(booking(status = "Confirmed").statusAllowsInspectionAct())
        assertFalse(booking(status = "ContractSignedByOwner").statusAllowsInspectionAct())
        assertTrue(booking(status = "ContractSignedByBoth").statusAllowsInspectionAct())
    }

    @Test
    fun statusAllowsInspectionAct_hidesPrepaidBooking() {
        assertFalse(
            booking(
                status = "PrePaid",
                prepaymentPaidAt = "2026-05-28T12:00:00Z",
                balanceDueAmount = 9000.0,
            ).statusAllowsInspectionAct(),
        )
    }

    @Test
    fun statusAllowsInspectionAct_allowsPaidBooking() {
        assertTrue(
            booking(
                status = "Paid",
                prepaymentPaidAt = "2026-05-28T12:00:00Z",
                balanceDueAmount = 0.0,
            ).statusAllowsInspectionAct(),
        )
    }

    @Test
    fun visibleInspectionActTypes_paidShowsOnlyHandover() {
        assertEquals(
            listOf(InspectionActType.Handover),
            booking(status = "Paid").visibleInspectionActTypes(),
        )
    }

    @Test
    fun visibleInspectionActTypes_contractSignedByBothShowsHandover() {
        assertEquals(
            listOf(InspectionActType.Handover),
            booking(status = "ContractSignedByBoth").visibleInspectionActTypes(),
        )
    }

    @Test
    fun visibleInspectionActTypes_activeWithoutSignedHandoverShowsOnlyHandover() {
        assertEquals(
            listOf(InspectionActType.Handover),
            booking(
                status = "Active",
                handoverActStatus = InspectionActStatus.Draft,
            ).visibleInspectionActTypes(),
        )
    }

    @Test
    fun visibleInspectionActTypes_activeWithSignedHandoverShowsHandoverAndReturn() {
        assertEquals(
            listOf(InspectionActType.Handover, InspectionActType.Return),
            booking(
                status = "Active",
                handoverActStatus = InspectionActStatus.SignedByBoth,
            ).visibleInspectionActTypes(),
        )
    }

    @Test
    fun visibleInspectionActTypes_completedWithoutSignedHandoverShowsOnlyHandover() {
        assertEquals(
            listOf(InspectionActType.Handover),
            booking(status = "Completed").visibleInspectionActTypes(),
        )
    }

    @Test
    fun visibleInspectionActTypes_completedWithSignedHandoverShowsHandoverAndReturn() {
        assertEquals(
            listOf(InspectionActType.Handover, InspectionActType.Return),
            booking(
                status = "Completed",
                handoverActStatus = InspectionActStatus.SignedByBoth,
                canOpenReturnInspection = true,
            ).visibleInspectionActTypes(),
        )
    }

    @Test
    fun visibleInspectionActTypes_confirmedShowsNone() {
        assertEquals(
            emptyList(),
            booking(status = "Confirmed").visibleInspectionActTypes(),
        )
    }

    @Test
    fun canShowSignContract_visibleAfterConfirmedEvenWhenApiFlagFalse() {
        val ownerView =
            booking(
                status = "Confirmed",
                canSignContractAsOwner = false,
                contractSignedByOwner = false,
            )
        val renterView =
            booking(
                status = "Confirmed",
                canSignContractAsRenter = false,
                contractSignedByRenter = false,
            )

        assertTrue(ownerView.canShowSignContractAsOwner())
        assertTrue(renterView.canShowSignContractAsRenter())
    }

    @Test
    fun canShowSignContract_hiddenAfterPartySigned() {
        val booking =
            booking(
                status = "Confirmed",
                contractSignedByOwner = true,
            )

        assertFalse(booking.canShowSignContractAsOwner())
    }

    @Test
    fun canShowSignContract_stillUsesApiFlagWhenPaid() {
        val booking =
            booking(
                status = "Paid",
                canSignContractAsOwner = true,
                contractSignedByOwner = false,
            )

        assertTrue(booking.canShowSignContractAsOwner())
    }

    private fun booking(
        status: String = "Confirmed",
        prepaymentPaidAt: String? = null,
        prepaymentPercent: Double = 0.0,
        prepaymentAmount: Double = 0.0,
        canPayPrepayment: Boolean = false,
        canPayFullAmount: Boolean = false,
        canSignContractAsOwner: Boolean = false,
        canSignContractAsRenter: Boolean = false,
        contractSignedByOwner: Boolean = false,
        contractSignedByRenter: Boolean = false,
        totalAmountWithDeposit: Double = 0.0,
        balanceDueAmount: Double = 0.0,
        canOpenReturnInspection: Boolean = false,
        handoverActStatus: InspectionActStatus = InspectionActStatus.None,
        returnActStatus: InspectionActStatus = InspectionActStatus.None,
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
            canPayFullAmount = canPayFullAmount,
            canSignContractAsOwner = canSignContractAsOwner,
            canSignContractAsRenter = canSignContractAsRenter,
            contractSignedByOwner = contractSignedByOwner,
            contractSignedByRenter = contractSignedByRenter,
            canOpenReturnInspection = canOpenReturnInspection,
            handoverActStatus = handoverActStatus,
            returnActStatus = returnActStatus,
            status = status,
            createdAt = "2026-05-27T10:00:00Z",
        )
}
