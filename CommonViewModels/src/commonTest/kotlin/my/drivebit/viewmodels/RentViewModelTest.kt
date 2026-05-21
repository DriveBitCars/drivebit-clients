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
import my.drivebit.network.services.PayBookingResult
import my.drivebit.network.services.Payment
import my.drivebit.shared.storage.Storage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeStorage(
    private val isLoggedIn: Boolean = true,
) : Storage {
    override fun isLogined() = isLoggedIn

    override fun saveToken(token: String) {}

    override fun getToken(): String? = null

    override fun saveRefreshToken(token: String) {}

    override fun getRefreshToken(): String? = null

    override fun logout() {}

    override fun putString(
        key: String,
        value: String,
    ) {}

    override fun getString(
        key: String,
        defaultValue: String,
    ): String = defaultValue

    override fun contains(key: String): Boolean = false

    override fun remove(key: String) {}
}

private class FakeBooking(
    private val calculateResult: (CheckBookingAvailabilityRequest) -> CheckBookingAvailabilityResponse,
    private val createStatus: String = "Pending",
) : Booking {
    var calculateCalls = mutableListOf<CheckBookingAvailabilityRequest>()

    override suspend fun calculate(request: CheckBookingAvailabilityRequest): CheckBookingAvailabilityResponse {
        calculateCalls.add(request)
        return calculateResult(request)
    }

    override suspend fun getMyAsRenter() = emptyList<my.drivebit.network.services.BookingDTO>()

    override suspend fun getMyAsOwner() = throw NotImplementedError()

    override suspend fun confirmAsOwner(bookingId: String) {}

    override suspend fun declineAsOwner(bookingId: String) {}

    override suspend fun getContract(bookingId: String): my.drivebit.network.services.GetBookingContractResult =
        throw NotImplementedError()

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
            status = createStatus,
            statusTranslate = null,
            createdAt = "2025-02-16T10:00:00Z",
        )
}

private class FakePayment : Payment {
    var registerCalls = 0

    override suspend fun registerBookingPayment(
        bookingId: String,
        returnUrl: String,
        failUrl: String,
    ): PayBookingResult {
        registerCalls++
        return PayBookingResult.Failed("unused in test")
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class RentViewModelTest {
    @Test
    fun `onBookClick sets showStartDateError when startDate is null`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val booking =
                FakeBooking(
                    calculateResult = { CheckBookingAvailabilityResponse(isAvailable = true, estimatedPrice = 1000.0) },
                )
            val storage = FakeStorage(isLoggedIn = true)
            val viewModel =
                RentViewModelImpl(
                    booking = booking,
                    payment = FakePayment(),
                    storage = storage,
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
            val booking =
                FakeBooking(
                    calculateResult = { CheckBookingAvailabilityResponse(isAvailable = true, estimatedPrice = 1000.0) },
                )
            val storage = FakeStorage(isLoggedIn = true)
            val viewModel =
                RentViewModelImpl(
                    booking = booking,
                    payment = FakePayment(),
                    storage = storage,
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
            val booking =
                FakeBooking(
                    calculateResult = { CheckBookingAvailabilityResponse(isAvailable = true, estimatedPrice = 1000.0) },
                )
            val storage = FakeStorage(isLoggedIn = true)
            val viewModel =
                RentViewModelImpl(
                    booking = booking,
                    payment = FakePayment(),
                    storage = storage,
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
            val booking =
                FakeBooking(
                    calculateResult = { CheckBookingAvailabilityResponse(isAvailable = true, estimatedPrice = 2000.0) },
                )
            val storage = FakeStorage(isLoggedIn = true)
            val viewModel =
                RentViewModelImpl(
                    booking = booking,
                    payment = FakePayment(),
                    storage = storage,
                    carId = "car-1",
                    coroutineScope = testScope,
                )

            viewModel.setStartDate("2025-02-16T10:00:00Z")
            advanceUntilIdle()
            viewModel.setEndDate("2025-02-18T10:00:00Z")
            advanceUntilIdle()
            viewModel.onBookClick()
            advanceUntilIdle()

            val created = viewModel.state.value as RentState.Book
            assertEquals("booking-1", created.pendingPaymentBookingId)
            assertTrue(created.pendingBookingPaymentUi is PendingBookingPaymentUi.AwaitingOwnerConfirmation)
            assertFalse(created.isCreating)
            assertFalse(created.showStartDateError)
            assertFalse(created.showEndDateError)
            viewModel.requestNavigateToMyBookings()
            assertTrue(viewModel.state.value is RentState.NavigateToMyBookings)
            viewModel.consumeNavigationEvent()
            val state = viewModel.state.value as RentState.Book
            assertFalse(state.showStartDateError)
            assertFalse(state.showEndDateError)
        }

    @Test
    fun `onBookClick sets ReadyToPay when create returns Confirmed`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val booking =
                FakeBooking(
                    calculateResult = { CheckBookingAvailabilityResponse(isAvailable = true, estimatedPrice = 2000.0) },
                    createStatus = "Confirmed",
                )
            val storage = FakeStorage(isLoggedIn = true)
            val viewModel =
                RentViewModelImpl(
                    booking = booking,
                    payment = FakePayment(),
                    storage = storage,
                    carId = "car-1",
                    coroutineScope = testScope,
                )

            viewModel.setStartDate("2025-02-16T10:00:00Z")
            advanceUntilIdle()
            viewModel.setEndDate("2025-02-18T10:00:00Z")
            advanceUntilIdle()
            viewModel.onBookClick()
            advanceUntilIdle()

            val created = viewModel.state.value as RentState.Book
            assertEquals("booking-1", created.pendingPaymentBookingId)
            assertTrue(created.pendingBookingPaymentUi is PendingBookingPaymentUi.ReadyToPay)
        }

    @Test
    fun `payCreatedBooking does nothing when awaiting owner confirmation`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val booking =
                FakeBooking(
                    calculateResult = { CheckBookingAvailabilityResponse(isAvailable = true, estimatedPrice = 2000.0) },
                )
            val storage = FakeStorage(isLoggedIn = true)
            val payment = FakePayment()
            val viewModel =
                RentViewModelImpl(
                    booking = booking,
                    payment = payment,
                    storage = storage,
                    carId = "car-1",
                    coroutineScope = testScope,
                )

            viewModel.setStartDate("2025-02-16T10:00:00Z")
            advanceUntilIdle()
            viewModel.setEndDate("2025-02-18T10:00:00Z")
            advanceUntilIdle()
            viewModel.onBookClick()
            advanceUntilIdle()
            viewModel.payCreatedBooking("https://ok", "https://fail")
            advanceUntilIdle()

            assertEquals(0, payment.registerCalls)
        }

    @Test
    fun `payCreatedBooking calls payment when ReadyToPay`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val booking =
                FakeBooking(
                    calculateResult = { CheckBookingAvailabilityResponse(isAvailable = true, estimatedPrice = 2000.0) },
                    createStatus = "Confirmed",
                )
            val storage = FakeStorage(isLoggedIn = true)
            val payment = FakePayment()
            val viewModel =
                RentViewModelImpl(
                    booking = booking,
                    payment = payment,
                    storage = storage,
                    carId = "car-1",
                    coroutineScope = testScope,
                )

            viewModel.setStartDate("2025-02-16T10:00:00Z")
            advanceUntilIdle()
            viewModel.setEndDate("2025-02-18T10:00:00Z")
            advanceUntilIdle()
            viewModel.onBookClick()
            advanceUntilIdle()
            viewModel.payCreatedBooking("https://ok", "https://fail")
            advanceUntilIdle()

            assertEquals(1, payment.registerCalls)
        }

    @Test
    fun `totalAmount and middlePrice are calculated when dates are selected`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val booking =
                FakeBooking(
                    calculateResult = { CheckBookingAvailabilityResponse(isAvailable = true, estimatedPrice = 3000.0) },
                )
            val storage = FakeStorage(isLoggedIn = true)
            val viewModel =
                RentViewModelImpl(
                    booking = booking,
                    payment = FakePayment(),
                    storage = storage,
                    carId = "car-1",
                    coroutineScope = testScope,
                )

            viewModel.setStartDate("2025-02-16T10:00:00Z")
            advanceUntilIdle()
            viewModel.setEndDate("2025-02-19T10:00:00Z")
            advanceUntilIdle()

            val state = viewModel.state.value as RentState.Book
            assertEquals("3000", state.totalAmount)
            assertEquals("1000", state.middlePrice)
        }

    @Test
    fun `middlePrice is totalAmount when single day rental`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val booking =
                FakeBooking(
                    calculateResult = { CheckBookingAvailabilityResponse(isAvailable = true, estimatedPrice = 1500.0) },
                )
            val storage = FakeStorage(isLoggedIn = true)
            val viewModel =
                RentViewModelImpl(
                    booking = booking,
                    payment = FakePayment(),
                    storage = storage,
                    carId = "car-1",
                    coroutineScope = testScope,
                )

            viewModel.setStartDate("2025-02-16T10:00:00Z")
            advanceUntilIdle()
            viewModel.setEndDate("2025-02-16T18:00:00Z")
            advanceUntilIdle()

            val state = viewModel.state.value as RentState.Book
            assertEquals("1500", state.totalAmount)
            assertEquals("1500", state.middlePrice)
        }

    @Test
    fun `onBookClick emits NavigateToLogin when not logged in`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val booking =
                FakeBooking(
                    calculateResult = { CheckBookingAvailabilityResponse(isAvailable = true, estimatedPrice = 2000.0) },
                )
            val storage = FakeStorage(isLoggedIn = false)
            val viewModel =
                RentViewModelImpl(
                    booking = booking,
                    payment = FakePayment(),
                    storage = storage,
                    carId = "car-123",
                    coroutineScope = testScope,
                )

            viewModel.setStartDate("2025-02-16T10:00:00Z")
            advanceUntilIdle()
            viewModel.setEndDate("2025-02-18T10:00:00Z")
            advanceUntilIdle()
            viewModel.onBookClick()
            advanceUntilIdle()

            val state = viewModel.state.value as RentState.NavigateToLogin
            assertEquals("car-123", state.carId)
        }

    @Test
    fun `middlePrice uses pricePerDay from API when present`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val booking =
                FakeBooking(calculateResult = {
                    CheckBookingAvailabilityResponse(
                        isAvailable = true,
                        estimatedPrice = 5400.0,
                        pricePerDay = 2700.0,
                        totalPrice = 5400.0,
                    )
                })
            val storage = FakeStorage(isLoggedIn = true)
            val viewModel =
                RentViewModelImpl(
                    booking = booking,
                    payment = FakePayment(),
                    storage = storage,
                    carId = "car-1",
                    coroutineScope = testScope,
                )

            viewModel.setStartDate("2026-03-18T22:55:00Z")
            advanceUntilIdle()
            viewModel.setEndDate("2026-03-20T18:00:00Z")
            advanceUntilIdle()

            val state = viewModel.state.value as RentState.Book
            assertEquals("5400", state.totalAmount)
            assertEquals("2700", state.middlePrice)
        }

    @Test
    fun `depositAmount is set when estimatedDeposit is non-zero`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val booking =
                FakeBooking(calculateResult = {
                    CheckBookingAvailabilityResponse(
                        isAvailable = true,
                        estimatedPrice = 3000.0,
                        pricePerDay = 1500.0,
                        totalPrice = 3000.0,
                        estimatedDeposit = 10000.0,
                    )
                })
            val storage = FakeStorage(isLoggedIn = true)
            val viewModel =
                RentViewModelImpl(
                    booking = booking,
                    payment = FakePayment(),
                    storage = storage,
                    carId = "car-1",
                    coroutineScope = testScope,
                )

            viewModel.setStartDate("2026-03-18T10:00:00Z")
            advanceUntilIdle()
            viewModel.setEndDate("2026-03-20T10:00:00Z")
            advanceUntilIdle()

            val state = viewModel.state.value as RentState.Book
            assertEquals("10000", state.depositAmount)
        }

    @Test
    fun `depositAmount is empty when estimatedDeposit is zero`() =
        runTest(StandardTestDispatcher()) {
            val testScope = CoroutineScope(SupervisorJob() + coroutineContext)
            val booking =
                FakeBooking(calculateResult = {
                    CheckBookingAvailabilityResponse(
                        isAvailable = true,
                        estimatedPrice = 5400.0,
                        pricePerDay = 2700.0,
                        totalPrice = 5400.0,
                        estimatedDeposit = 0.0,
                    )
                })
            val storage = FakeStorage(isLoggedIn = true)
            val viewModel =
                RentViewModelImpl(
                    booking = booking,
                    payment = FakePayment(),
                    storage = storage,
                    carId = "car-1",
                    coroutineScope = testScope,
                )

            viewModel.setStartDate("2026-03-18T22:55:00Z")
            advanceUntilIdle()
            viewModel.setEndDate("2026-03-20T18:00:00Z")
            advanceUntilIdle()

            val state = viewModel.state.value as RentState.Book
            assertEquals("", state.depositAmount)
        }
}
