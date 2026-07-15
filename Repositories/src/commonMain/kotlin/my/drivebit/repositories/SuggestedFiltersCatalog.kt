package my.drivebit.repositories

import my.drivebit.network.services.EnumItem
import my.drivebit.network.services.FilterSuggestion

object SuggestedFiltersCatalog {
    val suggested: List<FilterSuggestion> =
        listOf(
            FilterSuggestion(
                name = "Поездка в Крым",
                shortName = "В Крым",
                iconUrl = "/images/filter-main/travelling.svg",
                availableMileagePerDayKmMin = 150,
                dailyPriceMax = 6000,
            ),
            FilterSuggestion(
                name = "Поездка в Беларусь",
                shortName = "Беларусь",
                iconUrl = "/images/filter-main/parents.svg",
                availableMileagePerDayKmMin = 150,
                dailyPriceMax = 4500,
                engineTypes = listOf(EnumItem(number = 1, name = "Diesel", translate = "Дизель")),
            ),
            FilterSuggestion(
                name = "Поездка в Абхазию",
                shortName = "Абхазия",
                iconUrl = "/images/filter-main/work.svg",
                availableMileagePerDayKmMin = 200,
                dailyPriceMax = 4000,
            ),
            FilterSuggestion(
                name = "Премиум",
                shortName = "Премиум",
                iconUrl = "/images/filter-main/dacha.svg",
                dailyPriceMin = 7000,
            ),
            FilterSuggestion(
                name = "Эконом",
                shortName = "Эконом",
                iconUrl = "/images/filter-main/holidays.svg",
                dailyPriceMax = 2500,
            ),
            FilterSuggestion(
                name = "Комфорт",
                shortName = "Комфорт",
                iconUrl = "/images/filter-main/work.svg",
                dailyPriceMin = 2500,
                dailyPriceMax = 4000,
            ),
            FilterSuggestion(
                name = "Бизнес",
                shortName = "Бизнес",
                iconUrl = "/images/filter-main/citigroup.svg",
                dailyPriceMin = 4000,
                dailyPriceMax = 7000,
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
