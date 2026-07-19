package my.drivebit.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SearchDateRangeNavigationTest {
    @Test
    fun `partial start only does not navigate`() {
        assertNull(
            resolveSearchDateRangeNavigation(
                appliedStart = null,
                appliedEnd = null,
                draftStart = "2026-07-20",
                draftEnd = null,
            ),
        )
    }

    @Test
    fun `partial end only does not navigate`() {
        assertNull(
            resolveSearchDateRangeNavigation(
                appliedStart = null,
                appliedEnd = null,
                draftStart = null,
                draftEnd = "2026-07-25",
            ),
        )
    }

    @Test
    fun `complete pair navigates with both dates`() {
        assertEquals(
            SearchDateRangeUpdate(startDate = "2026-07-20", endDate = "2026-07-25"),
            resolveSearchDateRangeNavigation(
                appliedStart = null,
                appliedEnd = null,
                draftStart = "2026-07-20",
                draftEnd = "2026-07-25",
            ),
        )
    }

    @Test
    fun `same complete pair as applied does not navigate again`() {
        assertNull(
            resolveSearchDateRangeNavigation(
                appliedStart = "2026-07-20",
                appliedEnd = "2026-07-25",
                draftStart = "2026-07-20",
                draftEnd = "2026-07-25",
            ),
        )
    }

    @Test
    fun `clearing both dates navigates clear when previous range applied`() {
        assertEquals(
            SearchDateRangeUpdate(startDate = null, endDate = null),
            resolveSearchDateRangeNavigation(
                appliedStart = "2026-07-20",
                appliedEnd = "2026-07-25",
                draftStart = null,
                draftEnd = null,
            ),
        )
    }

    @Test
    fun `already clear stays without navigation`() {
        assertNull(
            resolveSearchDateRangeNavigation(
                appliedStart = null,
                appliedEnd = null,
                draftStart = null,
                draftEnd = null,
            ),
        )
    }

    @Test
    fun `replacing complete pair navigates new range`() {
        assertEquals(
            SearchDateRangeUpdate(startDate = "2026-08-01", endDate = "2026-08-10"),
            resolveSearchDateRangeNavigation(
                appliedStart = "2026-07-20",
                appliedEnd = "2026-07-25",
                draftStart = "2026-08-01",
                draftEnd = "2026-08-10",
            ),
        )
    }

    @Test
    fun `restarting range after complete pair waits for new end`() {
        assertNull(
            resolveSearchDateRangeNavigation(
                appliedStart = "2026-07-20",
                appliedEnd = "2026-07-25",
                draftStart = "2026-08-01",
                draftEnd = null,
            ),
        )
    }
}
