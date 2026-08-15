package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import my.drivebit.network.services.Booking
import my.drivebit.network.services.BookingContractDownloadDto
import my.drivebit.network.services.BookingDTO
import my.drivebit.network.services.CheckBookingAvailabilityRequest
import my.drivebit.network.services.CheckBookingAvailabilityResponse
import my.drivebit.network.services.CreateBookingRequest
import my.drivebit.network.services.GetBookingContractResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

private class BookingContractFakeBooking(
    var contractResult: GetBookingContractResult =
        GetBookingContractResult.Success(
            BookingContractDownloadDto(
                contractNumber = 33,
                fileName = "dogovor.pdf",
                downloadUrl = "https://example.com/contract.pdf",
                urlExpiresAt = "2026-08-15T16:42:07Z",
                generatedAt = "2026-08-15T13:33:17Z",
            ),
        ),
) : Booking {
    override suspend fun calculate(request: CheckBookingAvailabilityRequest): CheckBookingAvailabilityResponse =
        throw NotImplementedError()

    override suspend fun getMyAsRenter(): List<BookingDTO> = throw NotImplementedError()

    override suspend fun getMyAsOwner(): List<BookingDTO> = throw NotImplementedError()

    override suspend fun getById(bookingId: String): BookingDTO = throw NotImplementedError()

    override suspend fun createAsRenter(request: CreateBookingRequest): BookingDTO = throw NotImplementedError()

    override suspend fun confirmAsOwner(bookingId: String) = throw NotImplementedError()

    override suspend fun declineAsOwner(bookingId: String) = throw NotImplementedError()

    override suspend fun signContractAsOwner(bookingId: String): BookingDTO = throw NotImplementedError()

    override suspend fun signContractAsRenter(bookingId: String): BookingDTO = throw NotImplementedError()

    override suspend fun getContract(bookingId: String): GetBookingContractResult {
        yield()
        return contractResult
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class BookingContractViewModelTest {
    @Test
    fun `loadContract success never emits Error before Ready`() =
        runTest(StandardTestDispatcher()) {
            val fakeBooking = BookingContractFakeBooking()
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val viewModel =
                BookingContractViewModelImpl(
                    booking = fakeBooking,
                    bookingId = "7df1b26e-1ff4-4790-b5a1-63086c990b50",
                    coroutineScope = testScope,
                )
            val states = mutableListOf<BookingContractUiState>()
            val collectJob =
                launch {
                    viewModel.uiState.collect { states.add(it) }
                }

            viewModel.loadContract()
            advanceUntilIdle()
            collectJob.cancel()

            assertTrue(
                states.none { it is BookingContractUiState.Error },
                "unexpected Error states: $states",
            )
            assertIs<BookingContractUiState.Ready>(states.last())
        }

    @Test
    fun `loadContract Failed emits Error with message`() =
        runTest(StandardTestDispatcher()) {
            val fakeBooking =
                BookingContractFakeBooking(
                    contractResult = GetBookingContractResult.Failed("Сервер недоступен"),
                )
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val viewModel =
                BookingContractViewModelImpl(
                    booking = fakeBooking,
                    bookingId = "booking-1",
                    coroutineScope = testScope,
                )

            viewModel.loadContract()
            advanceUntilIdle()

            val error = assertIs<BookingContractUiState.Error>(viewModel.uiState.value)
            assertEquals("Сервер недоступен", error.message)
            assertTrue(error.reasons.isEmpty())
        }

    @Test
    fun `loadContract DataIncomplete keeps reasons`() =
        runTest(StandardTestDispatcher()) {
            val fakeBooking =
                BookingContractFakeBooking(
                    contractResult =
                        GetBookingContractResult.DataIncomplete(
                            message = "Не хватает данных",
                            reasons = listOf("Паспорт", "Права"),
                        ),
                )
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val viewModel =
                BookingContractViewModelImpl(
                    booking = fakeBooking,
                    bookingId = "booking-1",
                    coroutineScope = testScope,
                )

            viewModel.loadContract()
            advanceUntilIdle()

            val error = assertIs<BookingContractUiState.Error>(viewModel.uiState.value)
            assertEquals("Не хватает данных", error.message)
            assertEquals(listOf("Паспорт", "Права"), error.reasons)
        }

    @Test
    fun `loadContract blank bookingId emits Error without request`() =
        runTest(StandardTestDispatcher()) {
            val fakeBooking = BookingContractFakeBooking()
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val viewModel =
                BookingContractViewModelImpl(
                    booking = fakeBooking,
                    bookingId = "  ",
                    coroutineScope = testScope,
                )

            viewModel.loadContract()
            advanceUntilIdle()

            val error = assertIs<BookingContractUiState.Error>(viewModel.uiState.value)
            assertEquals("Не указан номер бронирования", error.message)
        }
}
