package my.drivebit.search

import kotlin.test.Test
import kotlin.test.assertEquals

class ResolveSearchCityIdTest {
    @Test
    fun `moskva slug resolves to live Moscow city id used by home search`() {
        assertEquals("158835", resolveSearchCityId("moskva"))
        assertEquals("158835", resolveSearchCityId("Moskva"))
    }

    @Test
    fun `null or unknown slug defaults to Moscow live city id`() {
        assertEquals("158835", resolveSearchCityId(null))
        assertEquals("158835", resolveSearchCityId("unknown-city"))
    }

    @Test
    fun `rostov and nearby city slugs resolve to live dictionary ids`() {
        assertEquals("158833", resolveSearchCityId("rostov-na-donu"))
        assertEquals("158840", resolveSearchCityId("krasnogorsk"))
        assertEquals("158841", resolveSearchCityId("lyubertsy"))
    }
}
