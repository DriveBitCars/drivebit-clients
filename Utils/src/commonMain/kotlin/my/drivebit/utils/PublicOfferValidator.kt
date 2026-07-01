package my.drivebit.utils

object PublicOfferMetadata {
    const val TITLE = "ПУБЛИЧНАЯ ОФЕРТА"
    const val PLATFORM = "drivebit.ru"
    const val REVISION_DATE = "«30» марта 2026 г."
    const val EFFECTIVE_DATE = "«25» октября 2025 г."
    const val INN = "9727127969"
    const val OGRN = "1267700092813"
    const val KPP = "772701001"
    const val COMPANY_NAME = "ООО «Драйвбит»"

    val REQUIRED_SECTIONS =
        listOf(
            "2. Предмет Агентского договора",
            "3. Термины и определения",
            "6.3.1.",
            "8. Права и обязанности Пользователя",
            "10.5. Возвраты денежных средств",
            "12.6.2.",
            "13. Дополнительные услуги",
        )

    val REQUIRED_PHRASES =
        listOf(
            "акцепт",
            "НЕ ЯВЛЯЕТСЯ стороной Договора аренды",
            "законодательством Российской Федерации",
            "обеспечительного платежа",
        )
}

fun validatePublicOffer(text: String): List<String> {
    val normalized = text.replace("\r\n", "\n")
    val errors = mutableListOf<String>()

    if (!normalized.contains(PublicOfferMetadata.TITLE)) {
        errors += "Missing offer title"
    }
    if (!normalized.contains(PublicOfferMetadata.PLATFORM)) {
        errors += "Missing platform domain"
    }
    if (!normalized.contains(PublicOfferMetadata.REVISION_DATE)) {
        errors += "Missing or outdated revision date"
    }
    if (!normalized.contains(PublicOfferMetadata.EFFECTIVE_DATE)) {
        errors += "Missing effective date"
    }
    if (!normalized.contains(PublicOfferMetadata.INN)) {
        errors += "Missing company INN"
    }
    if (!normalized.contains(PublicOfferMetadata.OGRN)) {
        errors += "Missing company OGRN"
    }
    if (!normalized.contains(PublicOfferMetadata.KPP)) {
        errors += "Missing company KPP"
    }

    PublicOfferMetadata.REQUIRED_SECTIONS.forEach { section ->
        if (!normalized.contains(section)) {
            errors += "Missing section: $section"
        }
    }

    PublicOfferMetadata.REQUIRED_PHRASES.forEach { phrase ->
        if (!normalized.contains(phrase, ignoreCase = true)) {
            errors += "Missing phrase: $phrase"
        }
    }

    if (normalized.contains("«16» апреля 2026")) {
        errors += "Contains outdated revision date (16 April 2026)"
    }

    return errors
}

fun isPublicOfferValid(text: String): Boolean = validatePublicOffer(text).isEmpty()
