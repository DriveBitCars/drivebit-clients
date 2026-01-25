package my.drivebit.components

import my.drivebit.network.services.CarAddress
import my.drivebit.network.services.CarGeneral
import my.drivebit.network.services.CarItem
import kotlin.test.Test
import kotlin.test.assertEquals

class CarItemSmallTitleTest {
    private fun general(
        brandName: String? = null,
        modelName: String? = null,
        year: Int? = null,
    ) = CarGeneral(
        brandName = brandName,
        modelName = modelName,
        year = year,
        address = CarAddress(geoLat = 0.0, geoLon = 0.0),
    )
    @Test
    fun `should format title with brand model and year`() {
        val car =
            CarItem(
                id = "test-id-123",
                general =
                    general(
                        brandName = "Toyota",
                        modelName = "Prius",
                        year = 2023,
                    ),
            )

        val title = formatCarTitle(car)

        assertEquals("Toyota Prius 2023", title)
    }

    @Test
    fun `should format title with only brand and model`() {
        val car =
            CarItem(
                id = "test-id-123",
                general =
                    general(
                        brandName = "BMW",
                        modelName = "X5",
                        year = null,
                    ),
            )

        val title = formatCarTitle(car)

        assertEquals("BMW X5", title)
    }

    @Test
    fun `should format title with only brand and year`() {
        val car =
            CarItem(
                id = "test-id-123",
                general =
                    general(
                        brandName = "Mercedes",
                        modelName = null,
                        year = 2022,
                    ),
            )

        val title = formatCarTitle(car)

        assertEquals("Mercedes 2022", title)
    }

    @Test
    fun `should format title with only model and year`() {
        val car =
            CarItem(
                id = "test-id-123",
                general =
                    general(
                        brandName = null,
                        modelName = "Civic",
                        year = 2021,
                    ),
            )

        val title = formatCarTitle(car)

        assertEquals("Civic 2021", title)
    }

    @Test
    fun `should format title with only brand`() {
        val car =
            CarItem(
                id = "test-id-123",
                general =
                    general(
                        brandName = "Audi",
                        modelName = null,
                        year = null,
                    ),
            )

        val title = formatCarTitle(car)

        assertEquals("Audi", title)
    }

    @Test
    fun `should format title with only model`() {
        val car =
            CarItem(
                id = "test-id-123",
                general =
                    general(
                        brandName = null,
                        modelName = "Corolla",
                        year = null,
                    ),
            )

        val title = formatCarTitle(car)

        assertEquals("Corolla", title)
    }

    @Test
    fun `should format title with only year`() {
        val car =
            CarItem(
                id = "test-id-123",
                general =
                    general(
                        brandName = null,
                        modelName = null,
                        year = 2020,
                    ),
            )

        val title = formatCarTitle(car)

        assertEquals("2020", title)
    }

    @Test
    fun `should use fallback when all fields are empty`() {
        val car =
            CarItem(
                id = "a575c0b1-3736-475f-a4a8-5a87cfbdb18a",
                general =
                    general(
                        brandName = null,
                        modelName = null,
                        year = null,
                    ),
            )

        val title = formatCarTitle(car)

        assertEquals("Автомобиль #a575c0b1", title)
    }

    @Test
    fun `should use fallback when general is empty`() {
        val car =
            CarItem(
                id = "test-id-456",
                general = general(),
            )

        val title = formatCarTitle(car)

        assertEquals("Автомобиль #test-id-", title)
    }

    @Test
    fun `should ignore empty strings`() {
        val car =
            CarItem(
                id = "test-id-123",
                general =
                    general(
                        brandName = "",
                        modelName = "   ",
                        year = 0,
                    ),
            )

        val title = formatCarTitle(car)

        assertEquals("Автомобиль #test-id-", title)
    }

    @Test
    fun `should ignore zero year`() {
        val car =
            CarItem(
                id = "test-id-123",
                general =
                    general(
                        brandName = "Toyota",
                        modelName = "Prius",
                        year = 0,
                    ),
            )

        val title = formatCarTitle(car)

        assertEquals("Toyota Prius", title)
    }

    @Test
    fun `should ignore negative year`() {
        val car =
            CarItem(
                id = "test-id-123",
                general =
                    general(
                        brandName = "Toyota",
                        modelName = "Prius",
                        year = -1,
                    ),
            )

        val title = formatCarTitle(car)

        assertEquals("Toyota Prius", title)
    }

    @Test
    fun `should handle whitespace in brand and model names`() {
        val car =
            CarItem(
                id = "test-id-123",
                general =
                    general(
                        brandName = "  Toyota  ",
                        modelName = "  Prius  ",
                        year = 2023,
                    ),
            )

        val title = formatCarTitle(car)

        assertEquals("  Toyota     Prius   2023", title)
    }

    @Test
    fun `should format title with all fields in correct order`() {
        val car =
            CarItem(
                id = "test-id-123",
                general =
                    general(
                        brandName = "Volkswagen",
                        modelName = "Golf",
                        year = 2024,
                    ),
            )

        val title = formatCarTitle(car)

        assertEquals("Volkswagen Golf 2024", title)
    }
}
