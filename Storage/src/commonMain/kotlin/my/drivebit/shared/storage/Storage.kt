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
}
