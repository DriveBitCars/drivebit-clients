package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

val NEARBY_RADIUS_OPTIONS_KM = listOf(5, 10, 25, 50)

@Composable
fun NearbyRadiusSelector(
    selectedRadiusKm: Int,
    onRadiusChange: (Int) -> Unit,
) {
    FilterButtonsRow {
        NEARBY_RADIUS_OPTIONS_KM.forEach { radiusKm ->
            UniversalButton(
                isSelected = selectedRadiusKm == radiusKm,
                onClick = { onRadiusChange(radiusKm) },
            ) {
                Span({
                    style {
                        applyTypography(CSSTypography.Styles.button)
                        property("pointer-events", "none")
                    }
                }) {
                    Text("$radiusKm км")
                }
            }
        }
    }
}
