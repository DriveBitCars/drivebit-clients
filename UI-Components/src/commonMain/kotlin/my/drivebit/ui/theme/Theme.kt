package my.drivebit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object ColorsDriveBit {
    val Blue = Color(0xFF2080FA)
    val BlueHover = Color(0xFF1A6DE0)

    val BlueRed = Color(0xFF0D2551)
    val Red = Color(0xFFEC4899)

    val White = Color(0xFFFFFFFF)
    /** Primary text — Figma gray-main */
    val Black = Color(0xFF152032)
    val Gray300 = Color(0xFFE0E0E6)
    val Gray600 = Color(0xFF4B5563)
    /** Figma gray-4 */
    val Gray4 = Color(0xFF737984)
    /** Figma gray-5 */
    val Gray5 = Color(0xFFA1A6AD)
    /** Figma gray-6 */
    val Gray6 = Color(0xFFD0D2D6)
    /** Figma gray-7 */
    val Gray7 = Color(0xFFF5F6F9)
    /** Master-button disabled */
    val DisabledBg = Color(0xFFE8EAEF)
    val BorderSubtle = Color(0xFFD8DDE6)
}

private val DarkColorScheme: ColorScheme =
    darkColorScheme(
        primary = ColorsDriveBit.Blue,
        onPrimary = ColorsDriveBit.White,
        background = ColorsDriveBit.Black,
        surface = ColorsDriveBit.Black,
        onSurface = ColorsDriveBit.White,
        surfaceVariant = ColorsDriveBit.Gray300,
        surfaceContainer = ColorsDriveBit.Black,
        outline = ColorsDriveBit.Blue,
    )

private val LightColorScheme: ColorScheme =
    lightColorScheme(
        primary = ColorsDriveBit.Blue,
        onPrimary = ColorsDriveBit.White,
        background = ColorsDriveBit.White,
        surface = ColorsDriveBit.White,
        onSurface = ColorsDriveBit.Black,
        surfaceVariant = ColorsDriveBit.Gray300,
        surfaceContainer = ColorsDriveBit.White,
        outline = ColorsDriveBit.Blue,
    )

@Composable
fun DrivebitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme =
        if (darkTheme) {
            DarkColorScheme
        } else {
            LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content,
    )
}
