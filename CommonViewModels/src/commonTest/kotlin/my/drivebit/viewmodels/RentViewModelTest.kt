package my.drivebit.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import my.drivebit.network.services.Booking
import my.drivebit.network.services.CheckBookingAvailabilityRequest
import my.drivebit.network.services.CheckBookingAvailabilityResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeBooking(
    private val calculateResult: (CheckBookingAvailabilityRequest) -> CheckBookingAvailabilityResponse,
) : Booking {
    var calculateCalls = mutableListOf<CheckBookingAvailabilityRequest>()

    override suspend fun calculate(request: CheckBookingAvailabilityRequest): CheckBookingAvailabilityResponse {
        calculateCalls.add(request)
        return calculateResult(request)
    }

    override suspend fun getMyAsRenter() = throw NotImplementedError()

    override suspend fun createAsRenter(request: my.drivebit.network.services.CreateBookingRequest) =
        my.drivebit.network.services.BookingDTO(
            id = "booking-1",
            carId = request.carId,
            carBrandName = null,
            carModelName = null,
            renterId = "renter-1",
            renterName = null,
            renterPhone = null,
            ownerId = "owner-1",
            ownerName = null,
            ownerPhone = null,
            startAt = request.startAt,
            endAt = request.endAt,
            totalAmount = 0.0,
            status = "created",
            createdAt = "2025-02-16T10:00:00Z",
        )
}

@OptIn(ExperimentalCoroutinesApi::class)
class RentViewModelTest {
    @Test
    fun `onBookClick sets showStartDateError when startDate is null`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val booking = FakeBooking { CheckBookingAvailabilityResponse(isAvailable = true, estimatedPrice = 1000.0) }
            val viewModel =
                RentViewModelImpl(
                    booking = booking,
                    carId = "car-1",
                    coroutineScope = testScope,
                )

            viewModel.setEndDate("2025-02-18T10:00:00Z")
            advanceUntilIdle()
            viewModel.onBookClick()
            advanceUntilIdle()

            val state = viewModel.state.value as RentState.Book
            assertTrue(state.showStartDateError)
            assertFalse(state.showEndDateError)
        }

    @Test
    fun `onBookClick sets showEndDateError when endDate is null`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val booking = FakeBooking { CheckBookingAvailabilityResponse(isAvailable = true, estimatedPrice = 1000.0) }
            val viewModel =
                RentViewModelImpl(
                    booking = booking,
                    carId = "car-1",
                    coroutineScope = testScope,
                )

            viewModel.setStartDate("2025-02-16T10:00:00Z")
            advanceUntilIdle()
            viewModel.onBookClick()
            advanceUntilIdle()

            val state = viewModel.state.value as RentState.Book
            assertFalse(state.showStartDateError)
            assertTrue(state.showEndDateError)
        }

    @Test
    fun `onBookClick sets both errors when both dates are null`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val booking = FakeBooking { CheckBookingAvailabilityResponse(isAvailable = true, estimatedPrice = 1000.0) }
            val viewModel =
                RentViewModelImpl(
                    booking = booking,
                    carId = "car-1",
                    coroutineScope = testScope,
                )

            viewModel.onBookClick()
            advanceUntilIdle()

            val state = viewModel.state.value as RentState.Book
            assertTrue(state.showStartDateError)
            assertTrue(state.showEndDateError)
        }

    @Test
    fun `onBookClick clears errors when both dates are set`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val booking = FakeBooking { CheckBookingAvailabilityResponse(isAvailable = true, estimatedPrice = 2000.0) }
            val viewModel =
                RentViewModelImpl(
                    booking = booking,
                    carId = "car-1",
                    coroutineScope = testScope,
                )

            viewModel.setStartDate("2025-02-16T10:00:00Z")
            advanceUntilIdle()
            viewModel.setEndDate("2025-02-18T10:00:00Z")
            advanceUntilIdle()
            viewModel.onBookClick()
            advanceUntilIdle()

            assertTrue(viewModel.state.value is RentState.NavigateToMyBookings)
            viewModel.consumeNavigationEvent()
            val state = viewModel.state.value as RentState.Book
            assertFalse(state.showStartDateError)
            assertFalse(state.showEndDateError)
        }

    @Test
    fun `totalAmount and middlePrice are calculated when dates are selected`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val booking =
                FakeBooking { CheckBookingAvailabilityResponse(isAvailable = true, estimatedPrice = 3000.0) }
            val viewModel =
                RentViewModelImpl(
                    booking = booking,
                    carId = "car-1",
                    coroutineScope = testScope,
                )

            viewModel.setStartDate("2025-02-16T10:00:00Z")
            advanceUntilIdle()
            viewModel.setEndDate("2025-02-19T10:00:00Z")
            advanceUntilIdle()

            val state = viewModel.state.value as RentState.Book
            assertEquals("3000.0", state.totalAmount)
            assertEquals("1000.0", state.middlePrice)
        }

    @Test
    fun `middlePrice is totalAmount when single day rental`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val booking = FakeBooking { CheckBookingAvailabilityResponse(isAvailable = true, estimatedPrice = 1500.0) }
            val viewModel =
                RentViewModelImpl(
                    booking = booking,
                    carId = "car-1",
                    coroutineScope = testScope,
                )

            viewModel.setStartDate("2025-02-16T10:00:00Z")
            advanceUntilIdle()
            viewModel.setEndDate("2025-02-16T18:00:00Z")
            advanceUntilIdle()

            val state = viewModel.state.value as RentState.Book
            assertEquals("1500.0", state.totalAmount)
            assertEquals("1500.0", state.middlePrice)
        }
}
