package my.drivebit.navigation

import my.drivebit.network.services.InspectionActType
import my.drivebit.network.services.inspectionActPagePath
import my.drivebit.network.services.inspectionActPageUrl
import my.drivebit.web.parseCityPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InspectionActRouteTest {
    @Test
    fun `inspection act is an account bundle route`() {
        assertTrue(isAccountBundlePath("/inspection-act"))
        assertTrue(isAccountBundlePath("/inspection-act/anything"))
        assertTrue("inspection-act" in accountBundleShellRoutes)
    }

    @Test
    fun `inspection act URL includes booking and type`() {
        assertEquals(
            "/inspection-act?bookingId=booking-1&type=Handover",
            inspectionActPagePath("booking-1", InspectionActType.Handover),
        )
        assertEquals(
            "https://drivebit.ru/inspection-act?bookingId=booking-1&type=Return",
            inspectionActPageUrl("booking-1", InspectionActType.Return),
        )
    }

    @Test
    fun `inspection act path is not a city slug`() {
        assertNull(parseCityPath("/inspection-act"))
        assertNull(parseCityPath("/inspection-act?bookingId=abc&type=Handover"))
    }
}
