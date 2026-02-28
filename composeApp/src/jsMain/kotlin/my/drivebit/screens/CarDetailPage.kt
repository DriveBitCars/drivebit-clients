package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import my.drivebit.components.AppWithHeader
import my.drivebit.components.CarBook
import my.drivebit.components.CarDescription
import my.drivebit.components.CarLocationMap
import my.drivebit.components.CarOwnerSection
import my.drivebit.components.CarPhotosSection
import my.drivebit.components.CarSpecsRow
import my.drivebit.components.CarTitleSection
import my.drivebit.components.Column
import my.drivebit.components.DailyRateLabel
import my.drivebit.components.Loader
import my.drivebit.components.Row
import my.drivebit.components.TextError
import my.drivebit.utils.getUrlParameter
import my.drivebit.viewmodels.CarDetailState
import my.drivebit.viewmodels.CarDetailViewModel
import my.drivebit.viewmodels.CarOwnerUi
import my.drivebit.viewmodels.RentViewModel
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.FlexWrap
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.flex
import org.jetbrains.compose.web.css.flexWrap
import org.jetbrains.compose.web.css.minWidth
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.koin.compose.currentKoinScope
import org.koin.core.parameter.parametersOf

@Composable
@Suppress("FunctionName")
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
                    CarDetailContent(
                        car = currentState.car,
                        owner = currentState.owner,
                        koinScope = koinScope,
                    )
                }
            }
        }
    }
}

@Composable
@Suppress("FunctionName")
private fun CarDetailContent(
    car: my.drivebit.network.services.CarDetailResponse,
    owner: CarOwnerUi?,
    koinScope: org.koin.core.scope.Scope,
) {
    Column(gap = 24.px) {
        CarPhotosSection(car)

        Row(
            gap = 24.px,
            flexWrap = FlexWrap.Wrap,
            alignItems = AlignItems.FlexStart,
        ) {
            Column(
                gap = 16.px,
                modifier = {
                    flex(2)
                    minWidth(0.px)
                },
            ) {
                val carName = "${car.resolvedBrandName()} ${car.resolvedModelName()}".trim()
                val carYear = car.resolvedProductionYear()

                CarTitleSection(
                    carName = carName,
                    carYear = carYear,
                )

                val dailyRate = car.resolvedDailyRate()
                DailyRateLabel(dailyRate)

                CarSpecsRow(car)

                CarDescription(description = car.general.description)

                owner?.let { ownerInfo ->
                    CarOwnerSection(
                        name = ownerInfo.name,
                        avatarUrl = ownerInfo.avatarUrl,
                        memberSince = ownerInfo.memberSince,
                        rating = null,
                        tripsCount = null,
                    )
                }
            }

            CarBook(
                viewModel =
                    remember(car.id) {
                        koinScope.get<RentViewModel>(parameters = { parametersOf(car.id) })
                    },
                carBookings = car.carBookings,
            )
        }

        val carLat = car.general.address.geoLat
        val carLon = car.general.address.geoLon

        if (carLat != null && carLon != null && carLat != 0.0 && carLon != 0.0) {
            CarLocationMap(car = car)
        }
    }
}
