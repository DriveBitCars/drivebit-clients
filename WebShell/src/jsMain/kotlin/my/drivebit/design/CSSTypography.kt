package my.drivebit.design

import org.jetbrains.compose.web.css.*

/**
 * CSS типографика для Compose Web
 * Использует Inter шрифт из Google Fonts
 */
object CSSTypography {
    // Основной шрифт
    val FontFamily = "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif"

    object FontSize {
        val xs: CSSSizeValue<CSSUnit.px> get() = 12.px
        val sm: CSSSizeValue<CSSUnit.px> get() = 14.px
        val base: CSSSizeValue<CSSUnit.px> get() = 16.px
        val lg: CSSSizeValue<CSSUnit.px> get() = 18.px
        val xl: CSSSizeValue<CSSUnit.px> get() = 20.px
        val xxl: CSSSizeValue<CSSUnit.px> get() = 24.px
        val xxxl: CSSSizeValue<CSSUnit.px> get() = 32.px
        val xxxxl: CSSSizeValue<CSSUnit.px> get() = 42.px
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
