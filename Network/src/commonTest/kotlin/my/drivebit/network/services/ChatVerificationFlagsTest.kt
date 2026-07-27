package my.drivebit.network.services

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChatVerificationFlagsTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

    @Test
    fun `chat participant deserializes verification flags`() {
        val participant =
            json.decodeFromString(
                ChatParticipantDto.serializer(),
                """
                {
                  "id": "user-1",
                  "name": "Антон",
                  "avatar": null,
                  "isPassportVerified": true,
                  "isDriverLicenseVerified": false
                }
                """.trimIndent(),
            )

        assertTrue(participant.isPassportVerified)
        assertFalse(participant.isDriverLicenseVerified)
    }

    @Test
    fun `message sender deserializes verification flags`() {
        val sender =
            json.decodeFromString(
                MessageSenderDto.serializer(),
                """
                {
                  "id": "user-2",
                  "name": "Иван",
                  "isPassportVerified": true,
                  "isDriverLicenseVerified": true
                }
                """.trimIndent(),
            )

        assertTrue(sender.isPassportVerified)
        assertTrue(sender.isDriverLicenseVerified)
    }

    @Test
    fun `chat participant defaults verification flags when absent`() {
        val participant =
            json.decodeFromString(
                ChatParticipantDto.serializer(),
                """{"id":"user-3","name":"Гость"}""",
            )

        assertFalse(participant.isPassportVerified)
        assertFalse(participant.isDriverLicenseVerified)
    }
}
