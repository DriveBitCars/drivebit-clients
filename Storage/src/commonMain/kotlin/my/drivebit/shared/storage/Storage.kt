package my.drivebit.shared.storage

/**
 * Простой интерфейс для работы с токеном авторизации
 */
interface Storage {
    /**
     * Проверяет, авторизован ли пользователь
     */
    fun isLogined(): Boolean

    /**
     * Сохраняет токен
     */
    fun saveToken(token: String)

    /**
     * Получает токен
     */
    fun getToken(): String?

    /**
     * Сохраняет refresh токен
     */
    fun saveRefreshToken(refreshToken: String)

    /**
     * Получает refresh токен
     */
    fun getRefreshToken(): String?

    /**
     * Выполняет выход из системы - очищает все данные пользователя
     */
    fun logout()

    /**
     * Сохраняет значение по ключу
     */
    fun putString(
        key: String,
        value: String,
    )

    /**
     * Получает значение по ключу
     */
    fun getString(
        key: String,
        defaultValue: String = "",
    ): String

    /**
     * Проверяет наличие ключа
     */
    fun contains(key: String): Boolean

    /**
     * Удаляет значение по ключу
     */
    fun remove(key: String)
}
