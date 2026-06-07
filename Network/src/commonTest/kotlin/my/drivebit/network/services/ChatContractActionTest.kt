package my.drivebit.network.services

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChatContractActionTest {
    @Test
    fun contractBookingIdForAction_returnsBookingIdForDownloadContractAction() {
        val message =
            MessageDto(
                id = "1",
                chatId = "c1",
                createdAt = "2026-05-28T12:00:00Z",
                isSystemMessage = true,
                messageActionBlock =
                    MessageActionBlockDto(
                        actionType = ActionTypeEnum.DownloadContract,
                        actionParameters = mapOf("bookingId" to "550e8400-e29b-41d4-a716-446655440000"),
                    ),
            )

        assertEquals("550e8400-e29b-41d4-a716-446655440000", message.contractBookingIdForAction())
    }

    @Test
    fun contractBookingIdForAction_parsesUrlFromTextWhenActionBlockMissing() {
        val message =
            MessageDto(
                id = "2",
                chatId = "c1",
                createdAt = "2026-05-28T12:00:00Z",
                isSystemMessage = true,
                text =
                    "Бронирование подтверждено.\n\n" +
                        "Скачать договор аренды: https://drivebit.ru/download-booking-contract?bookingId=660e8400-e29b-41d4-a716-446655440001",
            )

        assertEquals("660e8400-e29b-41d4-a716-446655440001", message.contractBookingIdForAction())
    }

    @Test
    fun contractBookingIdForAction_returnsNullForRegularMessage() {
        val message =
            MessageDto(
                id = "3",
                chatId = "c1",
                createdAt = "2026-05-28T12:00:00Z",
                text = "Привет",
            )

        assertNull(message.contractBookingIdForAction())
    }

    @Test
    fun payBookingIdForAction_doesNotReturnForDownloadContract() {
        val message =
            MessageDto(
                id = "4",
                chatId = "c1",
                createdAt = "2026-05-28T12:00:00Z",
                isSystemMessage = true,
                messageActionBlock =
                    MessageActionBlockDto(
                        actionType = ActionTypeEnum.DownloadContract,
                        actionParameters = mapOf("bookingId" to "550e8400-e29b-41d4-a716-446655440000"),
                    ),
            )

        assertNull(message.payBookingIdForAction())
    }

    @Test
    fun leaveReviewCarIdForAction_returnsCarIdFromParameters() {
        val message =
            MessageDto(
                id = "5",
                chatId = "c1",
                createdAt = "2026-05-28T12:00:00Z",
                isSystemMessage = true,
                messageActionBlock =
                    MessageActionBlockDto(
                        actionType = ActionTypeEnum.LeaveReviewForCar,
                        actionParameters = mapOf("carId" to "car-123"),
                    ),
            )

        assertEquals("car-123", message.leaveReviewCarIdForAction())
    }

    @Test
    fun leaveReviewCarIdForAction_resolvesCarIdFromBookingLookup() {
        val message =
            MessageDto(
                id = "6",
                chatId = "c1",
                createdAt = "2026-05-28T12:00:00Z",
                isSystemMessage = true,
                messageActionBlock =
                    MessageActionBlockDto(
                        actionType = ActionTypeEnum.LeaveReviewForCar,
                        actionParameters = mapOf("bookingId" to "booking-1"),
                    ),
            )
        val booking =
            BookingDTO(
                id = "booking-1",
                carId = "car-from-booking",
                renterId = "r1",
                ownerId = "o1",
                startAt = "2026-05-20T10:00:00Z",
                endAt = "2026-05-22T10:00:00Z",
                totalAmount = 1000.0,
                status = "Completed",
                createdAt = "2026-05-19T10:00:00Z",
            )

        assertEquals(
            "car-from-booking",
            message.leaveReviewCarIdForAction(mapOf("booking-1" to booking)),
        )
    }

    @Test
    fun leaveReviewCarIdForAction_usesBookingCompletedRenterTextFallback() {
        val message =
            MessageDto(
                id = "7",
                chatId = "c1",
                createdAt = "2026-05-28T12:00:00Z",
                isSystemMessage = true,
                systemMessageType = SystemMessageType.BookingCompleted,
                bookingId = "booking-2",
                text = "Ваша аренда автомобиля Toyota завершена. Вы можете оставить отзыв об аренде.",
            )
        val booking =
            BookingDTO(
                id = "booking-2",
                carId = "car-2",
                renterId = "r1",
                ownerId = "o1",
                startAt = "2026-05-20T10:00:00Z",
                endAt = "2026-05-22T10:00:00Z",
                totalAmount = 1000.0,
                status = "Completed",
                createdAt = "2026-05-19T10:00:00Z",
            )

        assertTrue(message.isLeaveReviewForCarAction())
        assertEquals("car-2", message.leaveReviewCarIdForAction(mapOf("booking-2" to booking)))
    }

    @Test
    fun shouldShowLeaveReviewForRenter_forOwnerBookingCompletedText() {
        val message =
            MessageDto(
                id = "8",
                chatId = "c1",
                createdAt = "2026-05-28T12:00:00Z",
                isSystemMessage = true,
                systemMessageType = SystemMessageType.BookingCompleted,
                text = "Аренда завершена. Вы можете оставить отзыв об арендаторе.",
            )

        assertTrue(message.shouldShowLeaveReviewForRenter())
        assertFalse(message.isLeaveReviewForCarAction())
    }

    @Test
    fun contractBookingIdForAction_returnsBookingIdForSignContractAction() {
        val message =
            MessageDto(
                id = "9",
                chatId = "c1",
                createdAt = "2026-05-28T12:00:00Z",
                isSystemMessage = true,
                messageActionBlock =
                    MessageActionBlockDto(
                        actionType = ActionTypeEnum.SignContract,
                        actionParameters = mapOf("bookingId" to "770e8400-e29b-41d4-a716-446655440002"),
                    ),
            )

        assertEquals("770e8400-e29b-41d4-a716-446655440002", message.contractBookingIdForAction())
    }

    @Test
    fun canShowSignContractInChat_usesCounterpartyToDetectRole() {
        val booking =
            BookingDTO(
                id = "booking-1",
                carId = "car-1",
                renterId = "renter-1",
                ownerId = "owner-1",
                startAt = "2026-05-20T10:00:00Z",
                endAt = "2026-05-22T10:00:00Z",
                totalAmount = 1000.0,
                status = "Confirmed",
                createdAt = "2026-05-19T10:00:00Z",
            )

        assertTrue(booking.canShowSignContractInChat(counterpartyUserId = "owner-1"))
        assertTrue(booking.canShowSignContractInChat(counterpartyUserId = "renter-1"))
        assertFalse(booking.canShowSignContractInChat(counterpartyUserId = "stranger"))
    }
}
