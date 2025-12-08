package my.drivebit.repositories

import my.drivebit.network.services.Auth
import my.drivebit.network.services.User
import my.drivebit.shared.storage.Storage

interface OtpResultRepository {
    suspend fun otpResult(
        identifier: String,
        code: String,
        additionalParams: Map<String, String> = emptyMap(),
    ): OtpResult
}

sealed interface OtpResult {
    data object Success : OtpResult

    data class Error(
        val message: String,
    ) : OtpResult
}

internal class VerifyOtpRepositoryImpl(
    private val auth: Auth,
    private val storage: Storage,
) : OtpResultRepository {
    override suspend fun otpResult(
        identifier: String,
        code: String,
        additionalParams: Map<String, String>,
    ): OtpResult {
        val result =
            runCatching {
                auth.verifyOtp(identifier, code)
            }

        return result.fold(
            onSuccess = { response ->
                storage.saveToken(response.accessToken.token)
                storage.saveRefreshToken(response.refreshToken.token)
                OtpResult.Success
            },
            onFailure = { throwable ->
                val message = throwable.message?.takeIf { it.isNotBlank() } ?: "Произошла ошибка"
                OtpResult.Error(message)
            },
        )
    }
}

internal class ChangeEmailRepositoryImpl(
    private val user: User,
) : OtpResultRepository {
    override suspend fun otpResult(
        identifier: String,
        code: String,
        additionalParams: Map<String, String>,
    ): OtpResult {
        val newLogin = additionalParams["newLogin"]!!
        val result =
            runCatching {
                user.changeEmail(identifier, code, newLogin)
            }

        return result.fold(
            onSuccess = {
                OtpResult.Success
            },
            onFailure = { throwable ->
                val message = throwable.message?.takeIf { it.isNotBlank() } ?: "Произошла ошибка"
                OtpResult.Error(message)
            },
        )
    }
}

internal class ChangePhoneRepositoryImpl(
    private val user: User,
) : OtpResultRepository {
    override suspend fun otpResult(
        identifier: String,
        code: String,
        additionalParams: Map<String, String>,
    ): OtpResult {
        val newLogin = additionalParams["newLogin"]!!
        val result =
            runCatching {
                user.changePhone(identifier, code, newLogin)
            }

        return result.fold(
            onSuccess = {
                OtpResult.Success
            },
            onFailure = { throwable ->
                val message = throwable.message?.takeIf { it.isNotBlank() } ?: "Произошла ошибка"
                OtpResult.Error(message)
            },
        )
    }
}
