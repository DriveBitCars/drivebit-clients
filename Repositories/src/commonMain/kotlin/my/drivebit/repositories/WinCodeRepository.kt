package my.drivebit.repositories

interface WinCodeRepository {
    fun saveWinCode(winCode: String)
    fun getWinCode(): String?
    fun clearWinCode()
}

internal class WinCodeRepositoryImpl : WinCodeRepository {
    private var winCode: String? = null

    override fun saveWinCode(winCode: String) {
        this.winCode = winCode
    }

    override fun getWinCode(): String? = winCode

    override fun clearWinCode() {
        winCode = null
    }
}

