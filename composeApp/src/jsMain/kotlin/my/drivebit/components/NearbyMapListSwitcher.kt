package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.viewmodels.NearbyLayoutMode
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun NearbyMapListSwitcher(
    mode: NearbyLayoutMode,
    onModeChange: (NearbyLayoutMode) -> Unit,
) {
    FilterButtonsRow {
        UniversalButton(
            isSelected = mode == NearbyLayoutMode.Map,
            onClick = { onModeChange(NearbyLayoutMode.Map) },
        ) {
            Span({
                style {
                    applyTypography(CSSTypography.Styles.button)
                    property("pointer-events", "none")
                }
            }) {
                Text("Карта")
            }
        }
        UniversalButton(
            isSelected = mode == NearbyLayoutMode.List,
            onClick = { onModeChange(NearbyLayoutMode.List) },
        ) {
            Span({
                style {
                    applyTypography(CSSTypography.Styles.button)
                    property("pointer-events", "none")
                }
            }) {
                Text("Список")
            }
        }
    }
}
