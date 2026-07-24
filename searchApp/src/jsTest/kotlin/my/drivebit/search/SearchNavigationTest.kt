package my.drivebit.search

import kotlin.test.Test
import kotlin.test.assertEquals

class SearchNavigationTest {
    @Test
    fun `reset preserves selected city after brand route removes city slug`() {
        val filters =
            SearchFilterSet(
                citySlug = null,
                brandSlug = "great-wall",
                modelSlug = "hover-m4",
            )

        val reset = resetSearchFilters(filters, selectedCitySlug = "rostov-na-donu")

        assertEquals(SearchFilterSet(citySlug = "rostov-na-donu"), reset)
    }
}
