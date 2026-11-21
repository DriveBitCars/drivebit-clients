package my.drivebit.design

import my.drivebit.ui.theme.ColorsDriveBit
import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.rgb

/**
 * CSS-версии цветов для использования в Compose Web
 * Используют только цвета из Theme.kt
 */
object CSSColors {
    // Основные цвета из Theme.kt
    val Black: CSSColorValue = ColorsDriveBit.Black.toCSSColor()
    val White: CSSColorValue = ColorsDriveBit.White.toCSSColor()
    val Gray300: CSSColorValue = ColorsDriveBit.Gray300.toCSSColor()
    val Gray600: CSSColorValue = ColorsDriveBit.Gray600.toCSSColor()
    val Gray4: CSSColorValue = ColorsDriveBit.Gray4.toCSSColor()
    val Gray5: CSSColorValue = ColorsDriveBit.Gray5.toCSSColor()
    val Gray6: CSSColorValue = ColorsDriveBit.Gray6.toCSSColor()
    val Gray7: CSSColorValue = ColorsDriveBit.Gray7.toCSSColor()
    val DisabledBg: CSSColorValue = ColorsDriveBit.DisabledBg.toCSSColor()
    val BorderSubtle: CSSColorValue = ColorsDriveBit.BorderSubtle.toCSSColor()

    // Акцентные цвета из Theme.kt
    val Blue: CSSColorValue = ColorsDriveBit.Blue.toCSSColor()
    val BlueHover: CSSColorValue = ColorsDriveBit.BlueHover.toCSSColor()
    val BlueRed: CSSColorValue = ColorsDriveBit.BlueRed.toCSSColor()
    val Red: CSSColorValue = ColorsDriveBit.Red.toCSSColor()
    val Green: CSSColorValue = rgb(34, 197, 94)

    // Строковые версии цветов для использования в setProperty
    val BlackString: String = ColorsDriveBit.Black.toHexString()
    val WhiteString: String = ColorsDriveBit.White.toHexString()
    val Gray300String: String = ColorsDriveBit.Gray300.toHexString()
    val Gray600String: String = ColorsDriveBit.Gray600.toHexString()
    val Gray4String: String = ColorsDriveBit.Gray4.toHexString()
    val Gray5String: String = ColorsDriveBit.Gray5.toHexString()
    val Gray6String: String = ColorsDriveBit.Gray6.toHexString()
    val Gray7String: String = ColorsDriveBit.Gray7.toHexString()
    val DisabledBgString: String = ColorsDriveBit.DisabledBg.toHexString()
    val BorderSubtleString: String = ColorsDriveBit.BorderSubtle.toHexString()
    val BlueString: String = ColorsDriveBit.Blue.toHexString()
    val BlueHoverString: String = ColorsDriveBit.BlueHover.toHexString()
    val BlueRedString: String = ColorsDriveBit.BlueRed.toHexString()
    val RedString: String = ColorsDriveBit.Red.toHexString()
}

/**
 * Расширение для преобразования Compose Color в CSSColorValue
 */
fun androidx.compose.ui.graphics.Color.toCSSColor(): CSSColorValue =
    rgb(
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt(),
    )

/**
 * Расширение для преобразования Compose Color в hex строку
 */
fun androidx.compose.ui.graphics.Color.toHexString(): String {
    val r = (red * 255).toInt()
    val g = (green * 255).toInt()
    val b = (blue * 255).toInt()
    return "#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}"
}
