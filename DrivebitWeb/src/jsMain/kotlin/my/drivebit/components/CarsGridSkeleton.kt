package my.drivebit.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Div

private const val SKELETON_CARD_COUNT = 9

@Composable
fun CarsGridSkeleton() {
    Div({
        classes("drivebit-cars-grid-static", "drivebit-cars-grid-compose")
        attr("role", "status")
        attr("aria-live", "polite")
        attr("aria-label", "Загрузка списка автомобилей")
    }) {
        Div({
            classes("drivebit-cars-grid-static__grid")
        }) {
            repeat(SKELETON_CARD_COUNT) {
                Div({
                    classes("drivebit-cars-grid-static__card")
                    attr("aria-hidden", "true")
                }) {
                    Div({ classes("drivebit-cars-grid-static__image") })
                    Div({
                        classes(
                            "drivebit-cars-grid-static__line",
                            "drivebit-cars-grid-static__line--title",
                        )
                    })
                    Div({
                        classes(
                            "drivebit-cars-grid-static__line",
                            "drivebit-cars-grid-static__line--price",
                        )
                    })
                }
            }
        }
    }
}
