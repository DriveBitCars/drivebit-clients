package my.drivebit.repositories

import my.drivebit.shared.storage.InMemorySettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SelectedAddressRepositoryTest {
    @Test
    fun `saveAddress should save address in memory`() {
        val repo = SelectedAddressRepositoryImpl(InMemorySettings())

        repo.saveAddress("Москва, ул. Ленина, д. 1")

        assertEquals("Москва, ул. Ленина, д. 1", repo.getAddress())
    }

    @Test
    fun `getAddress should return null when no address is saved`() {
        val repo = SelectedAddressRepositoryImpl(InMemorySettings())

        assertNull(repo.getAddress())
    }

    @Test
    fun `getAddress should return saved address`() {
        val repo = SelectedAddressRepositoryImpl(InMemorySettings())

        repo.saveAddress("Санкт-Петербург, Невский проспект, д. 10")
        val address = repo.getAddress()

        assertEquals("Санкт-Петербург, Невский проспект, д. 10", address)
    }

    @Test
    fun `clearAddress should remove saved address`() {
        val repo = SelectedAddressRepositoryImpl(InMemorySettings())

        repo.saveAddress("Москва, ул. Пушкина, д. 5")
        repo.clearAddress()

        assertNull(repo.getAddress())
    }

    @Test
    fun `saveAddress should overwrite previous address`() {
        val repo = SelectedAddressRepositoryImpl(InMemorySettings())

        repo.saveAddress("Москва, ул. Ленина, д. 1")
        repo.saveAddress("Санкт-Петербург, Невский проспект, д. 10")

        assertEquals("Санкт-Петербург, Невский проспект, д. 10", repo.getAddress())
    }

    @Test
    fun `saveAddress should save empty string`() {
        val repo = SelectedAddressRepositoryImpl(InMemorySettings())

        repo.saveAddress("")

        assertEquals("", repo.getAddress())
    }
}
