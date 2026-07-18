package my.drivebit.repositories

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import my.drivebit.shared.storage.InMemorySettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SearchFiltersRepositoryTest {
    @Test
    fun `should persist brand with search prefix across repository instances`() =
        runTest {
            val settings = InMemorySettings()
            val first = SearchFiltersRepositoryImpl(settings)

            first.updateBrand(42, "Toyota")

            assertTrue(settings.hasKey("search_filter_brand_id"))
            assertTrue(settings.hasKey("search_filter_brand_name"))
            assertEquals(42, settings.getInt("search_filter_brand_id", -1))
            assertEquals("Toyota", settings.getString("search_filter_brand_name", ""))

            val second = SearchFiltersRepositoryImpl(settings)
            assertEquals(42, second.brandId.first())
            assertEquals("Toyota", second.brandName.first())
        }

    @Test
    fun `should persist daily rate with search prefix`() =
        runTest {
            val settings = InMemorySettings()
            val repository = SearchFiltersRepositoryImpl(settings)

            repository.updateDailyRateMin(1000)
            repository.updateDailyRateMax(5000)

            assertEquals(1000, settings.getInt("search_daily_rate_min", -1))
            assertEquals(5000, settings.getInt("search_daily_rate_max", -1))

            val restored = SearchFiltersRepositoryImpl(settings)
            assertEquals(1000, restored.dailyRateMin.first())
            assertEquals(5000, restored.dailyRateMax.first())
        }

    @Test
    fun `should persist dates without search prefix`() =
        runTest {
            val settings = InMemorySettings()
            val first = SearchFiltersRepositoryImpl(settings)

            first.updateStartDate("2025-02-01")
            first.updateEndDate("2025-02-15")

            assertEquals("2025-02-01", settings.getString("start_date", ""))
            assertEquals("2025-02-15", settings.getString("end_date", ""))

            val second = SearchFiltersRepositoryImpl(settings)
            assertEquals("2025-02-01", second.startState.first())
            assertEquals("2025-02-15", second.endState.first())
        }

    @Test
    fun `should not persist currentTaskShortName across instances`() =
        runTest {
            val settings = InMemorySettings()
            val first = SearchFiltersRepositoryImpl(settings)
            first.updateCurrentTask("Премиум")

            val second = SearchFiltersRepositoryImpl(settings)
            assertNull(second.currentTaskShortName.first())
        }

    @Test
    fun `should not write nearby keys when updating filters`() =
        runTest {
            val settings = InMemorySettings()
            val repository = SearchFiltersRepositoryImpl(settings)

            repository.updateBrand(1, "BMW")
            repository.updateDailyRateMin(500)
            repository.updateStartDate("2025-03-01")
            repository.updateBodyType("suv", "Внедорожник")
            repository.updateSeatsMin(5)

            assertFalse(settings.hasKey("search_nearby_search_lat"))
            assertFalse(settings.hasKey("search_nearby_search_lon"))
            assertFalse(settings.hasKey("search_nearby_radius_km"))
            assertFalse(settings.hasKey("nearby_search_lat"))
            assertFalse(settings.hasKey("nearby_search_lon"))
            assertFalse(settings.hasKey("nearby_radius_km"))
        }

    @Test
    fun `should ignore stale nearby keys in settings on init`() =
        runTest {
            val settings = InMemorySettings()
            settings.putString("search_nearby_search_lat", "55.75")
            settings.putString("search_nearby_search_lon", "37.62")
            settings.putInt("search_nearby_radius_km", 25)

            SearchFiltersRepositoryImpl(settings)

            assertTrue(settings.hasKey("search_nearby_search_lat"))
            assertEquals(0, SearchFiltersRepositoryImpl(settings).currentPage.first())
        }

    @Test
    fun `should keep currentPage in memory only`() =
        runTest {
            val settings = InMemorySettings()
            val first = SearchFiltersRepositoryImpl(settings)
            first.setPage(3)

            val second = SearchFiltersRepositoryImpl(settings)
            assertEquals(0, second.currentPage.first())
        }
}
