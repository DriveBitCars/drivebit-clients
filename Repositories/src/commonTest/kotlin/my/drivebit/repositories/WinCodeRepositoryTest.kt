package my.drivebit.repositories

import my.drivebit.shared.storage.InMemorySettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WinCodeRepositoryTest {
    @Test
    fun `saveWinCode should save win code in memory`() {
        val repo = WinCodeRepositoryImpl(InMemorySettings())

        repo.saveWinCode("1HGBH41JXMN109186")

        assertEquals("1HGBH41JXMN109186", repo.getWinCode())
    }

    @Test
    fun `getWinCode should return null when no win code is saved`() {
        val repo = WinCodeRepositoryImpl(InMemorySettings())

        assertNull(repo.getWinCode())
    }

    @Test
    fun `getWinCode should return saved win code`() {
        val repo = WinCodeRepositoryImpl(InMemorySettings())

        repo.saveWinCode("ABCD1234EFGH56789")
        val winCode = repo.getWinCode()

        assertEquals("ABCD1234EFGH56789", winCode)
    }

    @Test
    fun `clearWinCode should remove saved win code`() {
        val repo = WinCodeRepositoryImpl(InMemorySettings())

        repo.saveWinCode("1HGBH41JXMN109186")
        repo.clearWinCode()

        assertNull(repo.getWinCode())
    }

    @Test
    fun `saveWinCode should overwrite previous win code`() {
        val repo = WinCodeRepositoryImpl(InMemorySettings())

        repo.saveWinCode("1HGBH41JXMN109186")
        repo.saveWinCode("ABCD1234EFGH56789")

        assertEquals("ABCD1234EFGH56789", repo.getWinCode())
    }

    @Test
    fun `saveWinCode should save empty string`() {
        val repo = WinCodeRepositoryImpl(InMemorySettings())

        repo.saveWinCode("")

        assertNull(repo.getWinCode())
    }
}
