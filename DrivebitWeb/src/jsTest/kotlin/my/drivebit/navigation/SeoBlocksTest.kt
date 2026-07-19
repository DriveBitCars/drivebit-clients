package my.drivebit.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SeoBlocksTest {
    @Test
    fun parseBlocksJson_moskvaFilters_areDistinct() {
        val blocks = SeoLandingBlocks.parseBlocksJson(FIXTURE_JSON)
        val moskva = blocks["/moskva"]
        val kanikuly = blocks["/moskva/kanikuly"]
        val komandirovki = blocks["/moskva/komandirovki"]

        assertNotNull(moskva)
        assertNotNull(kanikuly)
        assertNotNull(komandirovki)
        assertNotEquals(moskva.h2, kanikuly.h2)
        assertNotEquals(moskva.h2, komandirovki.h2)
        assertNotEquals(kanikuly.paragraphs.first(), komandirovki.paragraphs.first())
    }

    @Test
    fun normalizePath_trimsTrailingSlash() {
        assertEquals("/moskva/kanikuly", SeoLandingBlocks.normalizePath("/moskva/kanikuly/"))
    }

    @Test
    fun blockForPath_usesNormalizedKey() {
        val blocks = SeoLandingBlocks.parseBlocksJson(FIXTURE_JSON)
        val kanikuly = blocks[SeoLandingBlocks.normalizePath("/moskva/kanikuly/")]
        assertNotNull(kanikuly)
        assertEquals("Аренда авто для поездки в Крым и на каникулы", kanikuly.h2)
    }

    @Test
    fun renderSectionInnerHtml_rendersSectionsTableAndBullets() {
        val block =
            SeoLandingBlocks.parseBlocksJson(KRYM_FIXTURE_JSON)["/moskva/arenda-avto-v-krym"]!!
        val html = SeoLandingBlocks.renderSectionInnerHtml(block)
        assertTrue(html.contains("<h2>Почему поездка"))
        assertTrue(html.contains("<ul>"))
        assertTrue(html.contains("<table>"))
        assertTrue(html.contains("<th>Класс авто</th>"))
        assertTrue(html.contains("<td>Минивэн</td>"))
    }

    @Test
    fun parseBlocksJson_krasnogorskBlockExists() {
        val blocks = SeoLandingBlocks.parseBlocksJson(KRASNOGORSK_FIXTURE_JSON)
        val block = blocks["/krasnogorsk"]
        assertNotNull(block)
        assertEquals("Аренда авто у частных владельцев в Красногорске", block.h2)
    }

    @Test
    fun renderSectionInnerHtml_audiBrand_hasFourSectionsWithCyrillicQueries() {
        val block = SeoLandingBlocks.parseBlocksJson(AUDI_BRAND_FIXTURE_JSON)["/search/audi"]!!
        val sections = block.sections
        assertNotNull(sections)
        assertEquals(4, sections.size)
        val html = SeoLandingBlocks.renderSectionInnerHtml(block)
        assertTrue(html.contains("аренда ауди"))
        assertTrue(html.contains("аренда ауди в москве"))
        assertTrue(html.contains("аренда ауди без водителя"))
        assertTrue(html.contains("Как забронировать"))
        assertTrue(html.contains("<h2>"))
        assertTrue(!html.contains("аренда audi в москве"))
    }

    private companion object {
        val FIXTURE_JSON =
            """
            {
              "/moskva": {
                "ariaLabel": "Аренда внедорожника в Москве",
                "h2": "Аренда внедорожника в Москве без водителя",
                "paragraphs": ["p1", "p2"]
              },
              "/moskva/kanikuly": {
                "ariaLabel": "Аренда авто на каникулы",
                "h2": "Аренда авто для поездки в Крым и на каникулы",
                "paragraphs": ["k1", "k2"]
              },
              "/moskva/komandirovki": {
                "ariaLabel": "Аренда авто для командировок",
                "h2": "Аренда авто для командировки и поездки в другой город",
                "paragraphs": ["c1", "c2"]
              }
            }
            """.trimIndent()

        val KRYM_FIXTURE_JSON =
            """
            {
              "/moskva/arenda-avto-v-krym": {
                "ariaLabel": "Аренда авто для поездки в Крым",
                "h2": "Аренда авто для поездки в Крым из Москвы",
                "paragraphs": [],
                "sections": [
                  {
                    "heading": "Аренда авто для поездки в Крым из Москвы",
                    "paragraphs": ["intro"]
                  },
                  {
                    "heading": "Почему поездка в Крым на арендованном авто — удобный выбор",
                    "bullets": ["one", "two"]
                  },
                  {
                    "heading": "Какой автомобиль выбрать для поездки в Крым",
                    "table": {
                      "headers": ["Класс авто", "Примеры моделей"],
                      "rows": [["Минивэн", "Hyundai Starex"]]
                    }
                  }
                ]
              }
            }
            """.trimIndent()

        val KRASNOGORSK_FIXTURE_JSON =
            """
            {
              "/krasnogorsk": {
                "ariaLabel": "Аренда авто в Красногорске",
                "h2": "Аренда авто у частных владельцев в Красногорске",
                "paragraphs": ["p1", "p2"]
              }
            }
            """.trimIndent()

        val AUDI_BRAND_FIXTURE_JSON =
            """
            {
              "/search/audi": {
                "ariaLabel": "Аренда Audi в Москве",
                "h2": "Аренда Audi без водителя в Москве",
                "paragraphs": [],
                "sections": [
                  {
                    "heading": "Аренда Audi без водителя в Москве",
                    "paragraphs": [
                      "На DriveBit можно взять Audi в аренду в Москве напрямую у владельцев — без водителя и без переплаты классическому прокату. В каталоге — седаны, кроссоверы и другие модели для города, командировок и поездок по области. Популярные запросы: аренда ауди, аренда ауди в москве, аренда ауди без водителя, посуточная аренда Audi."
                    ]
                  },
                  {
                    "heading": "Какие модели Audi доступны",
                    "paragraphs": [
                      "Сравните предложения собственников: от компактных A3/A4 до кроссоверов Q5/Q7 и бизнес-седанов. У каждой машины — цена за сутки, пробег, залог и условия в карточке. Страховка и поддержка DriveBit включены в сервис."
                    ]
                  },
                  {
                    "heading": "Для каких поездок подходит Audi",
                    "paragraphs": [
                      "Деловые встречи и представительские поездки по Москве.",
                      "Комфортные маршруты по области и в аэропорт.",
                      "Посуточная аренда, когда нужна машина на выходные или короткий срок."
                    ]
                  },
                  {
                    "heading": "Как забронировать Audi",
                    "paragraphs": [
                      "Выберите модель, укажите даты и оформите заявку онлайн. Менеджер подтвердит бронь; также доступны Telegram и WhatsApp. Нужны паспорт РФ и водительское удостоверение."
                    ]
                  }
                ]
              }
            }
            """.trimIndent()
    }
}
