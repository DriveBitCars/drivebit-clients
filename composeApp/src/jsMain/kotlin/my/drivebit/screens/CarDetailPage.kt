package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import my.drivebit.components.AppWithHeader
import my.drivebit.components.CarPhotosSection
import my.drivebit.components.CarSpecsRow
import my.drivebit.components.CarTitleSection
import my.drivebit.components.Column
import my.drivebit.components.DailyRateLabel
import my.drivebit.components.Loader
import my.drivebit.components.SectionDivider
import my.drivebit.components.TextError
import my.drivebit.utils.getUrlParameter
import my.drivebit.viewmodels.CarDetailState
import my.drivebit.viewmodels.CarDetailViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.koin.compose.currentKoinScope
import org.koin.core.parameter.parametersOf

@Composable
fun CarDetailPage() {
    val carId = getUrlParameter("id")

    if (carId.isBlank()) {
        AppWithHeader {
            Div({
                style {
                    width(100.percent)
                    padding(20.px)
                    property("max-width", "1200px")
                    property("margin", "0 auto")
                }
            }) {
                TextError("Не указан ID автомобиля")
            }
        }
        return
    }

    val koinScope = currentKoinScope()
    val viewModel: CarDetailViewModel =
        remember(carId) {
            koinScope.get<CarDetailViewModel>(parameters = { parametersOf(carId) })
        }
    val state by viewModel.state.collectAsState()

    AppWithHeader {
        Div({
            style {
                width(100.percent)
                padding(20.px)
                property("max-width", "1200px")
                property("margin", "0 auto")
            }
        }) {
            when (val currentState = state) {
                is CarDetailState.Loading -> {
                    Loader()
                }

                is CarDetailState.Error -> {
                    TextError(currentState.message)
                }

                is CarDetailState.Success -> {
                    CarDetailContent(car = currentState.car)
                }
            }
        }
    }
}

@Composable
private fun CarDetailContent(car: my.drivebit.network.services.CarDetailResponse) {
    Column(gap = 24.px) {
        CarPhotosSection(car)

        Column(gap = 16.px) {
            val carName = "${car.resolvedBrandName()} ${car.resolvedModelName()}".trim()
            val carYear = car.resolvedProductionYear()

            CarTitleSection(
                carName = carName,
                carYear = carYear,
            )

            val dailyRate = car.resolvedDailyRate()
            DailyRateLabel(dailyRate)

            CarSpecsRow(car)

            SectionDivider()
        }
    }
}
