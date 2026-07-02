package my.drivebit.repositories

import my.drivebit.network.services.EnumItem
import my.drivebit.network.services.FilterSuggestion

object SuggestedFiltersCatalog {
    val suggested: List<FilterSuggestion> =
        listOf(
            FilterSuggestion(
                name = "Туризм / путешествие",
                shortName = "Путешествия",
                iconUrl = "/images/filter-main/travelling.svg",
                availableMileagePerDayKmMin = 150,
                dailyPriceMax = 6000,
            ),
            FilterSuggestion(
                name = "Навестить родных",
                shortName = "К родным",
                iconUrl = "/images/filter-main/parents.svg",
                availableMileagePerDayKmMin = 150,
                dailyPriceMax = 4500,
                engineTypes = listOf(EnumItem(number = 1, name = "Diesel", translate = "Дизель")),
            ),
            FilterSuggestion(
                name = "Работа / командировки",
                shortName = "Командировки",
                iconUrl = "/images/filter-main/work.svg",
                availableMileagePerDayKmMin = 200,
                dailyPriceMax = 4000,
            ),
            FilterSuggestion(
                name = "На дачу/загород",
                shortName = "За город",
                iconUrl = "/images/filter-main/dacha.svg",
                dailyPriceMax = 5000,
            ),
            FilterSuggestion(
                name = "Каникулы в Городе",
                shortName = "Каникулы",
                iconUrl = "/images/filter-main/holidays.svg",
                availableMileagePerDayKmMin = 150,
                dailyPriceMin = 10000,
                dailyPriceMax = 18000,
            ),
            FilterSuggestion(
                name = "Минивэн",
                shortName = "Минивэн",
                iconUrl = "/images/filter-main/house-moving-.svg",
                bodyTypes =
                    listOf(
                        EnumItem(number = 9, name = "Minivan", translate = "Минивэн"),
                    ),
            ),
            FilterSuggestion(
                name = "Внедорожник",
                shortName = "Внедорожник",
                iconUrl = "/images/filter-main/direction.svg",
                bodyTypes =
                    listOf(
                        EnumItem(number = 0, name = "SUV", translate = "Внедорожник"),
                    ),
            ),
        )

    fun findByShortName(shortName: String): FilterSuggestion? =
        suggested.firstOrNull { it.shortName == shortName }
}
