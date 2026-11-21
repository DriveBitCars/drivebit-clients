package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun CarYearLabel(year: Int) {
    if (year <= 0) return

    Div({
        classes("db-car-year")
    }) {
        Text("$year")
    }
}
