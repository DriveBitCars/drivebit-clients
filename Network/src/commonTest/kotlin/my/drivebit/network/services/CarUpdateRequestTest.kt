package my.drivebit.network.services

import my.drivebit.network.defaultJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CarUpdateRequestTest {
    @Test
    fun `toUpdateCarRequest maps insurance and omits create-only fields`() {
        val carId = "d5edee76-d7d7-42bd-be61-bfc3a73786c8"
        val createRequest =
            CarCreateRequest(
                id = carId,
                modelId = 2303,
                bodyType = "Sedan",
                driveType = "Front",
                engineType = "Gasoline",
                engineVolume = 1.6,
                year = 2015,
                seats = 4,
                trunkSize = "Medium",
                licensePlate = "A047MP77",
                ValidAddressString = "Ростов-на-Дону, Сосновая, 3Б",
                description = "Описание",
                hourlyRate = 66,
                dailyRate = 1599,
                deposit = 5000,
                prepaymentPercent = 10,
                availableMileagePerDayKm = 150,
                insurance = "OSAGO_Unlimited",
            )

        val updateRequest = createRequest.toUpdateCarRequest(carId)

        assertEquals(carId, updateRequest.carId)
        assertEquals("OSAGO_Unlimited", updateRequest.insurance)
        assertEquals(1599.0, updateRequest.dailyRate)
        assertEquals(10.0, updateRequest.prepaymentPercent)

        val json = defaultJson.encodeToString(UpdateCarRequest.serializer(), updateRequest)
        assertTrue(json.contains("\"carId\""))
        assertTrue(json.contains("\"insurance\":\"OSAGO_Unlimited\""))
        assertTrue(json.contains("\"prepaymentPercent\":10"))
        assertFalse(json.contains("\"id\""))
        assertFalse(json.contains("\"modelId\""))
        assertFalse(json.contains("\"seats\""))
        assertFalse(json.contains("parkingAssistances"))
        assertFalse(json.contains("multimediaSystemOptions"))
    }

    @Test
    fun `toUpdateCarRequest omits prepayment when not set on edit`() {
        val carId = "d5edee76-d7d7-42bd-be61-bfc3a73786c8"
        val createRequest =
            CarCreateRequest(
                year = 2015,
                dailyRate = 1599,
            )

        val updateRequest = createRequest.toUpdateCarRequest(carId)

        assertEquals(null, updateRequest.prepaymentPercent)
        val json = defaultJson.encodeToString(UpdateCarRequest.serializer(), updateRequest)
        assertFalse(json.contains("prepaymentPercent"))
    }

    @Test
    fun `toUpdateCarRequest omits unknown insurance values`() {
        val carId = "d5edee76-d7d7-42bd-be61-bfc3a73786c8"
        val createRequest =
            CarCreateRequest(
                year = 2015,
                insurance = "legacy_custom_value",
            )

        val updateRequest = createRequest.toUpdateCarRequest(carId)

        assertEquals(null, updateRequest.insurance)
        val json = defaultJson.encodeToString(UpdateCarRequest.serializer(), updateRequest)
        assertFalse(json.contains("insurance"))
    }
}
