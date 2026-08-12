package my.drivebit.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ContractFunnelAnalyticsTest {
    @Test
    fun goals_haveStableIdentifiersForMetrika() {
        assertEquals("contract_sign_click", ContractFunnelGoals.SIGN_CLICK)
        assertEquals("contract_sign_ok", ContractFunnelGoals.SIGN_OK)
        assertEquals("contract_sign_fail", ContractFunnelGoals.SIGN_FAIL)
        assertEquals("contract_download_click", ContractFunnelGoals.DOWNLOAD_CLICK)
        assertEquals("contract_download_ready", ContractFunnelGoals.DOWNLOAD_READY)
        assertEquals("contract_download_incomplete", ContractFunnelGoals.DOWNLOAD_INCOMPLETE)
        assertEquals("contract_download_fail", ContractFunnelGoals.DOWNLOAD_FAIL)
    }

    @Test
    fun signParams_includeSourceRoleAndBookingId() {
        val params =
            contractFunnelParams(
                source = ContractFunnelSource.Chat,
                role = ContractFunnelRole.Renter,
                bookingId = "87306748-e188-47a7-b35f-908d2b5ec224",
            )
        assertEquals("chat", params["source"])
        assertEquals("renter", params["role"])
        assertEquals("87306748-e188-47a7-b35f-908d2b5ec224", params["bookingId"])
        assertNull(params["message"])
    }

    @Test
    fun failParams_truncateLongMessage() {
        val long = "x".repeat(300)
        val params =
            contractFunnelParams(
                source = ContractFunnelSource.MyDeals,
                role = ContractFunnelRole.Owner,
                bookingId = "b1",
                message = long,
            )
        assertEquals(120, params.getValue("message").length)
        assertEquals("my_deals", params["source"])
        assertEquals("owner", params["role"])
    }

    @Test
    fun downloadParams_omitRoleWhenNull() {
        val params =
            contractFunnelParams(
                source = ContractFunnelSource.DownloadPage,
                role = null,
                bookingId = "b2",
            )
        assertEquals("download_page", params["source"])
        assertEquals("b2", params["bookingId"])
        assertNull(params["role"])
    }
}
