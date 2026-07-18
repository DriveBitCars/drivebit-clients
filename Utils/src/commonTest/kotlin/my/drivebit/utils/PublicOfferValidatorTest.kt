package my.drivebit.utils

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PublicOfferValidatorTest {
    @Test
    fun validatePublicOffer_rejectsEmptyText() {
        val errors = validatePublicOffer("")
        assertTrue(errors.isNotEmpty())
    }

    @Test
    fun validatePublicOffer_rejectsOutdatedRevisionDate() {
        val text =
            """
            ${PublicOfferMetadata.TITLE}
            Дата публикации действующей редакции: «16» апреля 2026 г.
            ${PublicOfferMetadata.INN}
            ${PublicOfferMetadata.OGRN}
            """.trimIndent()

        val errors = validatePublicOffer(text)
        assertTrue(errors.any { it.contains("revision date") })
    }

    @Test
    fun validatePublicOffer_acceptsMinimalValidDocument() {
        val text = buildMinimalValidOffer()
        assertTrue(isPublicOfferValid(text), validatePublicOffer(text).joinToString())
    }

    @Test
    fun validatePublicOffer_requiresAgentNonPartyClause() {
        val text =
            buildMinimalValidOffer().replace(
                "НЕ ЯВЛЯЕТСЯ стороной Договора аренды",
                "",
            )
        assertFalse(isPublicOfferValid(text))
    }

    @Test
    fun validatePublicOffer_requiresSection13() {
        val text = buildMinimalValidOffer().replace("13. Дополнительные услуги", "")
        assertFalse(isPublicOfferValid(text))
    }

    @Test
    fun validatePublicOffer_requiresCompanyIdentifiers() {
        val text = buildMinimalValidOffer().replace(PublicOfferMetadata.INN, "")
        assertFalse(isPublicOfferValid(text))
    }

    private fun buildMinimalValidOffer(): String =
        buildString {
            appendLine(PublicOfferMetadata.TITLE)
            appendLine("на оказание агентских услуг через «${PublicOfferMetadata.PLATFORM}»")
            appendLine("Дата публикации и вступления в силу: ${PublicOfferMetadata.EFFECTIVE_DATE}")
            appendLine("Дата публикации действующей редакции: ${PublicOfferMetadata.REVISION_DATE}")
            appendLine("Акцепт настоящей Оферты")
            appendLine("2. Предмет Агентского договора")
            appendLine("3. Термины и определения")
            appendLine(
                "${PublicOfferMetadata.COMPANY_NAME} ИНН ${PublicOfferMetadata.INN} ОГРН ${PublicOfferMetadata.OGRN} КПП ${PublicOfferMetadata.KPP}",
            )
            appendLine("6.3.1. Договор аренды")
            appendLine("8. Права и обязанности Пользователя")
            appendLine("10.5. Возвраты денежных средств")
            appendLine("12.6.2. Пользователь соглашается")
            appendLine("13. Дополнительные услуги")
            appendLine("НЕ ЯВЛЯЕТСЯ стороной Договора аренды")
            appendLine("законодательством Российской Федерации")
            appendLine("обеспечительного платежа")
        }
}
