package my.drivebit.viewmodels

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.NetworkException
import my.drivebit.network.services.Booking
import my.drivebit.network.services.BookingDTO
import my.drivebit.network.services.CheckBookingAvailabilityRequest
import my.drivebit.network.services.CheckBookingAvailabilityResponse
import my.drivebit.network.services.CreateBookingRequest
import my.drivebit.network.services.GetBookingContractResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class MyBookingsFakeBooking(
    var ownerBookings: List<BookingDTO> = emptyList(),
    var signResult: BookingDTO? = null,
    var refreshCalls: Int = 0,
) : Booking {
    override suspend fun calculate(request: CheckBookingAvailabilityRequest): CheckBookingAvailabilityResponse =
        throw NotImplementedError()

    override suspend fun getMyAsRenter(): List<BookingDTO> = throw NotImplementedError()

    override suspend fun getMyAsOwner(): List<BookingDTO> {
        refreshCalls++
        if (refreshCalls > 1) {
            throw NetworkException(HttpStatusCode.InternalServerError, "Refresh failed")
        }
        return ownerBookings
    }

    override suspend fun getById(bookingId: String): BookingDTO = throw NotImplementedError()

    override suspend fun createAsRenter(request: CreateBookingRequest): BookingDTO = throw NotImplementedError()

    override suspend fun confirmAsOwner(bookingId: String) = throw NotImplementedError()

    override suspend fun declineAsOwner(bookingId: String) = throw NotImplementedError()

    override suspend fun signContractAsOwner(bookingId: String): BookingDTO =
        signResult ?: ownerBookings.first { it.id == bookingId }.copy(contractSignedByOwner = true)

    override suspend fun signContractAsRenter(bookingId: String): BookingDTO = throw NotImplementedError()

    override suspend fun getContract(bookingId: String): GetBookingContractResult = throw NotImplementedError()
}

private fun sampleBooking(id: String = "booking-1"): BookingDTO =
    BookingDTO(
        id = id,
        carId = "car-1",
        renterId = "renter-1",
        ownerId = "owner-1",
        startAt = "2026-06-07T08:19:00Z",
        endAt = "2026-06-08T08:19:00Z",
        totalAmount = 10.0,
        status = "AwaitingOwnerSignature",
        createdAt = "2026-06-07T07:19:58.80443Z",
        canSignContractAsOwner = true,
    )

@OptIn(ExperimentalCoroutinesApi::class)
class MyBookingsAsOwnerViewModelTest {
    @Test
    fun `signContractAsOwner updates booking locally when refresh fails`() =
        runTest(StandardTestDispatcher()) {
            val bookingId = "booking-1"
            val initial = sampleBooking(bookingId)
            val signed =
                initial.copy(
                    contractSignedByOwner = true,
                    canSignContractAsOwner = false,
                    status = "ContractSignedByOwner",
                )
            val fakeBooking =
                MyBookingsFakeBooking(
                    ownerBookings = listOf(initial),
                    signResult = signed,
                )
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val viewModel = MyBookingsAsOwnerViewModelImpl(fakeBooking, testScope)

            viewModel.loadBookings()
            advanceUntilIdle()
            viewModel.signContractAsOwner(bookingId)
            advanceUntilIdle()

            assertNull(viewModel.error.value)
            val updatedBooking = viewModel.bookings.value.single()
            assertTrue(updatedBooking.contractSignedByOwner)
            assertFalse(updatedBooking.canSignContractAsOwner)
            assertEquals("ContractSignedByOwner", updatedBooking.status)
        }
}
