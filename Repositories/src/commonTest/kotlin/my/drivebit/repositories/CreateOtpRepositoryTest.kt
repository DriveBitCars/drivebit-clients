package my.drivebit.repositories

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.Auth
import my.drivebit.network.services.AuthOperationResponse
import my.drivebit.network.services.CreateOtpResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeAuth : Auth {
    var shouldThrow = false
    var errorMessage: String? = "Network error"
    var lastLogin: String? = null

    override suspend fun createOtp(login: String): CreateOtpResponse {
        lastLogin = login
        if (shouldThrow) {
            throw if (errorMessage != null) Exception(errorMessage) else Exception()
        }
        return CreateOtpResponse(
            message = "OK",
            sessionId = "session-id",
            expiresIn = 300,
        )
    }

    override suspend fun verifyOtp(
        identifier: String,
        code: String,
    ): my.drivebit.network.services.VerifyOtpResponse = throw NotImplementedError()

    override suspend fun login(
        login: String,
        password: String,
    ) = throw NotImplementedError()

    override suspend fun changePasswordViaOtp(
        identifier: String,
        code: String,
        newPassword: String,
    ): AuthOperationResponse = throw NotImplementedError()

    override suspend fun createTokens(refreshToken: String): my.drivebit.network.services.CreateNewTokensResponse =
        throw NotImplementedError()
}

@OptIn(ExperimentalCoroutinesApi::class)
class CreateOtpRepositoryTest {
    @Test
    fun `createOtp should return Success and propagate sessionId on success`() =
        runTest {
            val fakeAuth = FakeAuth()
            val repo = CreateOtpRepositoryImpl(fakeAuth)

            val result = repo.createOtp("login@example.com")

            assertTrue(result is ResultOtp.Success)
            assertEquals("session-id", result.sessionId)
            assertEquals("login@example.com", fakeAuth.lastLogin)
        }

    @Test
    fun `createOtp should return Error with message from exception`() =
        runTest {
            val fakeAuth =
                FakeAuth().apply {
                    shouldThrow = true
                    errorMessage = "Invalid login"
                }
            val repo = CreateOtpRepositoryImpl(fakeAuth)

            val result = repo.createOtp("bad-login")

            assertTrue(result is ResultOtp.Error)
            assertEquals("Invalid login", result.message)
        }

    @Test
    fun `createOtp should return default error message when exception message is null or blank`() =
        runTest {
            val fakeAuth =
                FakeAuth().apply {
                    shouldThrow = true
                    errorMessage = ""
                }
            val repo = CreateOtpRepositoryImpl(fakeAuth)

            val result = repo.createOtp("any")

            assertTrue(result is ResultOtp.Error)
            assertEquals("Произошла ошибка", result.message)
        }
}
