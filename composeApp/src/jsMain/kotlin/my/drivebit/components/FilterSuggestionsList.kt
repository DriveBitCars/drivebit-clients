package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.network.services.FilterSuggestion
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun FilterSuggestionsList(
    filters: List<FilterSuggestion>,
    onFilterClick: (FilterSuggestion) -> Unit,
) {
    Row(
        gap = 16.px,
        flexWrap = FlexWrap.Wrap,
    ) {
        filters.forEach { filter ->
            FilterSuggestionButton(
                filter = filter,
                onClick = {
                    onFilterClick(filter)
                },
            )
        }
    }
}

@Composable
private fun FilterSuggestionButton(
    filter: FilterSuggestion,
    onClick: () -> Unit,
) {
    UniversalButton(
        isSelected = false,
        onClick = onClick,
    ) {
        Span({
            style {
                applyTypography(CSSTypography.Styles.button)
                fontSize(CSSTypography.FontSize.base)
            }
        }) {
            Text(filter.title)
        }
    }
}
