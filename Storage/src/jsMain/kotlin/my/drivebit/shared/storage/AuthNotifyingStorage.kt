package my.drivebit.shared.storage

internal class AuthNotifyingStorage(
    private val delegate: Storage,
) : Storage {
    override fun isLogined(): Boolean = delegate.isLogined()

    override fun saveToken(token: String) {
        delegate.saveToken(token)
        dispatchAuthChangedEvent()
    }

    override fun getToken(): String? = delegate.getToken()

    override fun saveRefreshToken(refreshToken: String) {
        delegate.saveRefreshToken(refreshToken)
        dispatchAuthChangedEvent()
    }

    override fun getRefreshToken(): String? = delegate.getRefreshToken()

    override fun logout() {
        delegate.logout()
        dispatchAuthChangedEvent()
    }

    override fun putString(
        key: String,
        value: String,
    ) {
        delegate.putString(key, value)
        if (key == "my_city_name") {
            dispatchAuthChangedEvent()
        }
    }

    override fun getString(
        key: String,
        defaultValue: String,
    ): String = delegate.getString(key, defaultValue)

    override fun contains(key: String): Boolean = delegate.contains(key)

    override fun remove(key: String) {
        delegate.remove(key)
    }
}
