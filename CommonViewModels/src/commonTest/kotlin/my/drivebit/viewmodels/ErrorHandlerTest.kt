package my.drivebit.viewmodels

import io.ktor.http.HttpStatusCode
import my.drivebit.network.NetworkException
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorHandlerTest {
    @Test
    fun `humanizeApiError maps AddressNotFound to Russian message`() {
        assertEquals(
            "Адрес не найден. Укажите один город, улицу и дом — без лишних городов в строке.",
            ErrorHandler.humanizeApiError("AddressNotFound"),
        )
    }

    @Test
    fun `extractErrorMessage humanizes NetworkException`() {
        val message =
            ErrorHandler.extractErrorMessage(
                NetworkException(HttpStatusCode.BadRequest, "AddressNotFound"),
            )
        assertEquals(
            "Адрес не найден. Укажите один город, улицу и дом — без лишних городов в строке.",
            message,
        )
    }
}
