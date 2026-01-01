package my.drivebit.repositories

import com.russhwolf.settings.Settings

interface WinCodeRepository {
    fun saveWinCode(winCode: String)

    fun getWinCode(): String?

    fun clearWinCode()
}

internal class WinCodeRepositoryImpl(
    private val settings: Settings,
) : WinCodeRepository {
    companion object {
        private const val WIN_CODE_KEY = "win_code"
    }

    override fun saveWinCode(winCode: String) {
        settings.putString(WIN_CODE_KEY, winCode)
    }

    override fun getWinCode(): String? = settings.getStringOrNullIfEmpty(WIN_CODE_KEY)

    override fun clearWinCode() {
        settings.remove(WIN_CODE_KEY)
    }
}
