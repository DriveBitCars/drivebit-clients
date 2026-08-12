package my.drivebit.search

import kotlin.test.Test
import kotlin.test.assertEquals

class SearchFilterChipOrderTest {
    @Test
    fun `reset chip is first when filters are active`() {
        assertEquals(
            listOf(
                "Сбросить",
                "Марка",
                "Привод",
                "Кузов",
                "Количество мест",
                "Год выпуска",
                "Километраж",
                "Цена",
            ),
            searchFilterChipOrder(includeReset = true),
        )
    }

    @Test
    fun `reset chip is omitted when no filters are active`() {
        assertEquals(
            listOf(
                "Марка",
                "Привод",
                "Кузов",
                "Количество мест",
                "Год выпуска",
                "Километраж",
                "Цена",
            ),
            searchFilterChipOrder(includeReset = false),
        )
    }
}
