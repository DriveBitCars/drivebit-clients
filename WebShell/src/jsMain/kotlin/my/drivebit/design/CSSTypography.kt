package my.drivebit.design

import org.jetbrains.compose.web.css.*

/**
 * CSS типографика для Compose Web (Figma DriveBit)
 * Body: Roboto Regular · Headings: Inter Tight
 */
object CSSTypography {
    val FontFamily = "\"Roboto\", sans-serif"
    val FontFamilyDisplay = "\"Inter Tight\", \"Roboto\", sans-serif"

    object FontSize {
        val xs: CSSSizeValue<CSSUnit.px> get() = 12.px
        val sm: CSSSizeValue<CSSUnit.px> get() = 14.px
        val base: CSSSizeValue<CSSUnit.px> get() = 16.px
        val lg: CSSSizeValue<CSSUnit.px> get() = 18.px
        val xl: CSSSizeValue<CSSUnit.px> get() = 20.px
        val xxl: CSSSizeValue<CSSUnit.px> get() = 24.px
        val xxxl: CSSSizeValue<CSSUnit.px> get() = 32.px
        val xxxxl: CSSSizeValue<CSSUnit.px> get() = 42.px
        val h1: CSSSizeValue<CSSUnit.px> get() = 52.px
        val h2: CSSSizeValue<CSSUnit.px> get() = 48.px
        val h3: CSSSizeValue<CSSUnit.px> get() = 36.px
        val h4: CSSSizeValue<CSSUnit.px> get() = 32.px
        val h5: CSSSizeValue<CSSUnit.px> get() = 24.px
    }

    object FontWeight {
        val light = 300
        val normal = 400
        val medium = 500
        val semibold = 600
        val bold = 700
    }

    object Styles {
        val button: StyleScope.() -> Unit = {
            fontFamily(FontFamily)
            fontSize(FontSize.sm)
            property("font-weight", "500")
            lineHeight("1")
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

        val h1: StyleScope.() -> Unit = {
            fontFamily(FontFamilyDisplay)
            fontSize(FontSize.h1)
            fontWeight(FontWeight.semibold)
            lineHeight("1.09")
        }

        val h3: StyleScope.() -> Unit = {
            fontFamily(FontFamilyDisplay)
            fontSize(FontSize.h3)
            fontWeight(FontWeight.medium)
            lineHeight("1.2")
        }

        val h4: StyleScope.() -> Unit = {
            fontFamily(FontFamilyDisplay)
            fontSize(FontSize.h4)
            fontWeight(FontWeight.semibold)
            lineHeight("1.2")
        }
    }
}

/**
 * Расширения для удобного применения типографики
 */
fun StyleScope.applyTypography(style: StyleScope.() -> Unit) {
    style()
}
