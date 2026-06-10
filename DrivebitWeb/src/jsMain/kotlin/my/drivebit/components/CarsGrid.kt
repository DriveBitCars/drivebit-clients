package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.network.services.CarItem
import my.drivebit.web.buildCarDetailUrl
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.gridTemplateColumns
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div

@Composable
fun CarsGrid(
    cars: List<CarItem>,
    carHref: (CarItem) -> String = { car -> buildCarDetailUrl(car.id) },
) {
    Div({
        classes("drivebit-cars-grid-mounted")
        style {
            display(DisplayStyle.Grid)
            gridTemplateColumns("repeat(auto-fill, minmax(280px, 1fr))")
            gap(24.px)
            width(100.percent)
        }
    }) {
        cars.forEach { car ->
            Column {
                CarItemSmall(
                    car = car,
                    href = carHref(car),
                )
            }
        }
    }
}
