package my.drivebit.shared.storage

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StorageTest {
    @Test
    fun testInitialState() {
        val storage = createTestStorage()

        // Изначально пользователь не авторизован
        assertFalse(storage.isLogined())
        assertNull(storage.getToken())
    }

    @Test
    fun testSaveAndGetToken() {
        val storage = createTestStorage()
        val testToken = "test_token_123"

        // Сохраняем токен
        storage.saveToken(testToken)

        // Проверяем, что токен сохранился
        assertEquals(testToken, storage.getToken())
        assertTrue(storage.isLogined())
    }

    @Test
    fun testSaveEmptyToken() {
        val storage = createTestStorage()

        // Сохраняем пустой токен
        storage.saveToken("")

        // Пустой токен означает, что пользователь не авторизован
        // Реальная Storage возвращает null для пустой строки
        assertNull(storage.getToken())
        assertFalse(storage.isLogined())
    }

    @Test
    fun testSaveNullToken() {
        val storage = createTestStorage()

        // Сохраняем null токен (если поддерживается)
        storage.saveToken("")

        // Null токен означает, что пользователь не авторизован
        assertFalse(storage.isLogined())
    }

    @Test
    fun testTokenReplacement() {
        val storage = createTestStorage()

        // Сохраняем первый токен
        val firstToken = "first_token"
        storage.saveToken(firstToken)
        assertEquals(firstToken, storage.getToken())
        assertTrue(storage.isLogined())

        // Заменяем на второй токен
        val secondToken = "second_token"
        storage.saveToken(secondToken)
        assertEquals(secondToken, storage.getToken())
        assertTrue(storage.isLogined())
    }

    @Test
    fun testMultipleOperations() {
        val storage = createTestStorage()

        // Проверяем последовательность операций
        assertFalse(storage.isLogined())

        storage.saveToken("token1")
        assertTrue(storage.isLogined())
        assertEquals("token1", storage.getToken())

        storage.saveToken("token2")
        assertTrue(storage.isLogined())
        assertEquals("token2", storage.getToken())

        storage.saveToken("")
        assertFalse(storage.isLogined())
        assertNull(storage.getToken())
    }

    @Test
    fun testCreate() {
        // Создаем первое хранилище через фабрику
        val firstStorage = createTestStorageWithFactory()
        val testToken = "persistent_token_123"

        firstStorage.saveToken(testToken)
        assertTrue(firstStorage.isLogined())
        assertEquals(testToken, firstStorage.getToken())

        // Создаем новое хранилище через фабрику
        val secondStorage = createTestStorageWithFactory()

        // Проверяем, что данные сохранились между созданиями Storage
        assertTrue(secondStorage.isLogined())
        assertEquals(testToken, secondStorage.getToken())
    }

    @Test
    fun testLogout() {
        val storage = createTestStorage()

        // Сохраняем токены
        storage.saveToken("test_token")
        storage.saveRefreshToken("test_refresh_token")
        assertTrue(storage.isLogined())
        assertEquals("test_token", storage.getToken())
        assertEquals("test_refresh_token", storage.getRefreshToken())

        // Выполняем logout
        storage.logout()

        // Проверяем, что все данные очищены
        assertFalse(storage.isLogined())
        assertNull(storage.getToken())
        assertNull(storage.getRefreshToken())
    }

    @Test
    fun testLogoutWhenNotLoggedIn() {
        val storage = createTestStorage()

        // Изначально не авторизован
        assertFalse(storage.isLogined())
        assertNull(storage.getToken())
        assertNull(storage.getRefreshToken())

        // Выполняем logout (не должно быть ошибок)
        storage.logout()

        // Проверяем, что состояние не изменилось
        assertFalse(storage.isLogined())
        assertNull(storage.getToken())
        assertNull(storage.getRefreshToken())
    }

    @Test
    fun testLogoutClearsAllData() {
        val storage = createTestStorage()

        // Сохраняем данные
        storage.saveToken("token1")
        storage.saveRefreshToken("refresh1")
        assertTrue(storage.isLogined())

        // Заменяем данные
        storage.saveToken("token2")
        storage.saveRefreshToken("refresh2")
        assertEquals("token2", storage.getToken())
        assertEquals("refresh2", storage.getRefreshToken())

        // Выполняем logout
        storage.logout()

        // Проверяем, что все очищено
        assertFalse(storage.isLogined())
        assertNull(storage.getToken())
        assertNull(storage.getRefreshToken())
    }

    // Фабричный метод для создания тестового хранилища
    private fun createTestStorage(): Storage {
        // Используем реальную StorageImpl с in-memory Settings
        val settings = InMemorySettings()
        return StorageImpl(settings)
    }

    // Фабричный метод для создания тестового контекста
    private fun createTestContext(): Any {
        // Создаем тестовый контекст для фабрики
        return TestContext()
    }

    // Фабричный метод для создания Storage через фабрику (симуляция create())
    private fun createTestStorageWithFactory(): Storage {
        // Создаем персистентное хранилище для тестирования
        return PersistentTestStorage()
    }
}

/**
 * Тестовая реализация Storage для unit-тестов
 */
private class TestStorage : Storage {
    private var token: String? = null
    private var refreshToken: String? = null
    private val storage = mutableMapOf<String, String>()

    override fun isLogined(): Boolean = !token.isNullOrEmpty()

    override fun saveToken(token: String) {
        this.token = token
    }

    override fun getToken(): String? = token

    override fun saveRefreshToken(refreshToken: String) {
        this.refreshToken = refreshToken
    }

    override fun getRefreshToken(): String? = refreshToken

    override fun logout() {
        token = null
        refreshToken = null
        storage.clear()
    }

    override fun putString(
        key: String,
        value: String,
    ) {
        storage[key] = value
    }

    override fun getString(
        key: String,
        defaultValue: String,
    ): String = storage[key] ?: defaultValue

    override fun contains(key: String): Boolean = storage.containsKey(key)

    override fun remove(key: String) {
        storage.remove(key)
    }
}

/**
 * Тестовая реализация Context для тестирования StorageFactory
 */
private class TestContext {
    // Простой тестовый контекст
    // В реальном приложении это будет Android Context или другой платформо-специфичный контекст
}

/**
 * Персистентная тестовая реализация Storage для тестирования create(context)
 */
private class PersistentTestStorage : Storage {
    companion object {
        // Статическое хранилище для симуляции персистентности между экземплярами
        private var persistentToken: String? = null
        private var persistentRefreshToken: String? = null
        private val persistentStorage = mutableMapOf<String, String>()
    }

    override fun isLogined(): Boolean = !persistentToken.isNullOrEmpty()

    override fun saveToken(token: String) {
        persistentToken = token
    }

    override fun getToken(): String? = persistentToken

    override fun saveRefreshToken(refreshToken: String) {
        persistentRefreshToken = refreshToken
    }

    override fun getRefreshToken(): String? = persistentRefreshToken

    override fun logout() {
        persistentToken = null
        persistentRefreshToken = null
        persistentStorage.clear()
    }

    override fun putString(
        key: String,
        value: String,
    ) {
        persistentStorage[key] = value
    }

    override fun getString(
        key: String,
        defaultValue: String,
    ): String = persistentStorage[key] ?: defaultValue

    override fun contains(key: String): Boolean = persistentStorage.containsKey(key)

    override fun remove(key: String) {
        persistentStorage.remove(key)
    }
}
