package my.drivebit.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class SearchLocationSeoTest {
    @Test
    fun locationPathForSeo_stripsQueryFragmentAndTrailingSlash() {
        assertEquals("/search/audi", locationPathForSeo("/search/audi?page=2"))
        assertEquals("/search/audi", locationPathForSeo("/search/audi/#hash"))
        assertEquals("/search/audi", locationPathForSeo("/search/audi/"))
        assertEquals("/moskva/search", locationPathForSeo("/moskva/search"))
        assertEquals("/", locationPathForSeo("/"))
    }

    @Test
    fun commitSearchLocationSeo_invokesUpdaterWithNormalizedBrandPath() {
        var seen: String? = null
        commitSearchLocationSeo("/search/audi?foo=1") { path ->
            seen = path
        }
        assertEquals("/search/audi", seen)
    }
}
