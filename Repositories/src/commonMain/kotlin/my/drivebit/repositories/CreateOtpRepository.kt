package my.drivebit.repositories

import my.drivebit.network.services.Auth

interface CreateOtpRepository {
    suspend fun createOtp(login: String): ResultOtp
}

sealed interface ResultOtp {
    data class Success(
        val sessionId: String,
    ) : ResultOtp

    data class Error(
        val message: String,
    ) : ResultOtp
}

internal class CreateOtpRepositoryImpl(
    private val auth: Auth,
) : CreateOtpRepository {
    override suspend fun createOtp(login: String): ResultOtp {
        val result =
            runCatching {
                auth.createOtp(login)
            }

        return result.fold(
            onSuccess = { response ->
                ResultOtp.Success(response.sessionId)
            },
            onFailure = { throwable ->
                val errorMessage = throwable.message?.takeIf { it.isNotBlank() } ?: "Произошла ошибка"
                ResultOtp.Error(errorMessage)
            },
        )
    }
}
