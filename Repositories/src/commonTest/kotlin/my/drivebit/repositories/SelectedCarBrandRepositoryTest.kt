package my.drivebit.repositories

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SelectedCarBrandRepositoryTest {
    @Test
    fun `should save and retrieve brand`() {
        val repository = SelectedCarBrandRepositoryImpl()

        repository.saveBrand(1, "Toyota")

        assertEquals(1, repository.getBrandId())
        assertEquals("Toyota", repository.getBrandName())
    }

    @Test
    fun `should return null when no brand is saved`() {
        val repository = SelectedCarBrandRepositoryImpl()

        assertNull(repository.getBrandId())
        assertNull(repository.getBrandName())
    }

    @Test
    fun `should clear brand`() {
        val repository = SelectedCarBrandRepositoryImpl()

        repository.saveBrand(1, "Toyota")
        repository.clearBrand()

        assertNull(repository.getBrandId())
        assertNull(repository.getBrandName())
    }

    @Test
    fun `should overwrite existing brand`() {
        val repository = SelectedCarBrandRepositoryImpl()

        repository.saveBrand(1, "Toyota")
        repository.saveBrand(2, "BMW")

        assertEquals(2, repository.getBrandId())
        assertEquals("BMW", repository.getBrandName())
    }
}
