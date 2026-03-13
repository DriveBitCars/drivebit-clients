package my.drivebit.design

import org.jetbrains.compose.web.css.*

/**
 * CSS типографика для Compose Web
 * Использует Inter шрифт из Google Fonts
 */
object CSSTypography {
    // Основной шрифт
    val FontFamily = "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif"

    // Размеры шрифтов в пикселях
    object FontSize {
        val xs = 12.px
        val sm = 14.px
        val base = 16.px
        val lg = 18.px
        val xl = 20.px
        val xxl = 24.px
        val xxxl = 32.px
        val xxxxl = 42.px
    }

    // Веса шрифтов
    object FontWeight {
        val light = 300
        val normal = 400
        val medium = 500
        val semibold = 600
        val bold = 700
    }

    // Стили текста для CSS
    object Styles {
        val button: StyleScope.() -> Unit = {
            fontFamily(FontFamily)
            fontSize(FontSize.sm)
            fontWeight(FontWeight.medium)
            lineHeight("1.5")
        }

        val body: StyleScope.() -> Unit = {
            fontFamily(FontFamily)
            fontSize(FontSize.base)
            fontWeight(FontWeight.normal)
            lineHeight("1.5")
        }

        val caption: StyleScope.() -> Unit = {
            fontFamily(FontFamily)
            fontSize(FontSize.xs)
            fontWeight(FontWeight.normal)
            lineHeight("1.5")
        }
    }
}

/**
 * Расширения для удобного применения типографики
 */
fun StyleScope.applyTypography(style: StyleScope.() -> Unit) {
    style()
}
