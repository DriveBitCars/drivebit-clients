package my.drivebit.repositories

import my.drivebit.network.services.Auth
import my.drivebit.shared.storage.Storage

interface PasswordLoginRepository {
    suspend fun login(
        login: String,
        password: String,
    ): PasswordLoginResult
}

sealed interface PasswordLoginResult {
    data object Success : PasswordLoginResult

    data class Error(
        val message: String,
    ) : PasswordLoginResult
}

internal class PasswordLoginRepositoryImpl(
    private val auth: Auth,
    private val storage: Storage,
    private val avatarRepository: AvatarRepository,
) : PasswordLoginRepository {
    override suspend fun login(
        login: String,
        password: String,
    ): PasswordLoginResult {
        val result =
            runCatching {
                auth.login(login = login, password = password)
            }

        return result.fold(
            onSuccess = { response ->
                if (!response.success) {
                    val message = response.error?.takeIf { it.isNotBlank() } ?: "Произошла ошибка"
                    return@fold PasswordLoginResult.Error(message)
                }
                storage.saveToken(response.accessToken.token)
                storage.saveRefreshToken(response.refreshToken.token)
                avatarRepository.clearCache()
                PasswordLoginResult.Success
            },
            onFailure = { throwable ->
                val errorMessage = throwable.message?.takeIf { it.isNotBlank() } ?: "Произошла ошибка"
                PasswordLoginResult.Error(errorMessage)
            },
        )
    }
}
