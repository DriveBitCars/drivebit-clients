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

    // Акцентные цвета из Theme.kt
    val Blue: CSSColorValue = ColorsDriveBit.Blue.toCSSColor()
    val BlueRed: CSSColorValue = ColorsDriveBit.BlueRed.toCSSColor()
    val Red: CSSColorValue = ColorsDriveBit.Red.toCSSColor()

    // Строковые версии цветов для использования в setProperty
    val BlackString: String = ColorsDriveBit.Black.toHexString()
    val WhiteString: String = ColorsDriveBit.White.toHexString()
    val Gray300String: String = ColorsDriveBit.Gray300.toHexString()
    val Gray600String: String = ColorsDriveBit.Gray600.toHexString()
    val BlueString: String = ColorsDriveBit.Blue.toHexString()
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
