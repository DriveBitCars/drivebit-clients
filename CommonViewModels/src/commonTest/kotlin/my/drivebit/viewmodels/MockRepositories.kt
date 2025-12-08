package my.drivebit.viewmodels

import my.drivebit.repositories.CreateOtpRepository
import my.drivebit.repositories.ResultOtp

class MockCreateOtpRepository : CreateOtpRepository {
    var shouldThrowError = false
    var errorMessage = "Network error"
    var lastLoginCalled: String? = null

    override suspend fun createOtp(login: String): ResultOtp {
        lastLoginCalled = login
        if (shouldThrowError) {
            val message = errorMessage.takeIf { it.isNotBlank() } ?: "Произошла ошибка"
            return ResultOtp.Error(message)
        }
        return ResultOtp.Success("test-session-id-guid")
    }
}
