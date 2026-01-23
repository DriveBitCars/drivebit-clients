package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.browser.window
import my.drivebit.components.AppWithHeader
import my.drivebit.components.CarSpecChip
import my.drivebit.components.Loader
import my.drivebit.components.TextError
import my.drivebit.design.CSSColors
import my.drivebit.utils.getUrlParameter
import my.drivebit.viewmodels.CarDetailState
import my.drivebit.viewmodels.CarDetailViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
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
    val allPhotos = (car.photos + (car.general?.photos ?: emptyList())).distinctBy { it.id }
    val mainPhoto = allPhotos.firstOrNull()
    val thumbnailPhotos = allPhotos.drop(1).take(2)

    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            gap(24.px)
        }
    }) {
        Div({
            style {
                display(DisplayStyle.Flex)
                gap(16.px)
                width(100.percent)
            }
        }) {
            Div({
                style {
                    flex(1)
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    gap(8.px)
                }
            }) {
                if (mainPhoto != null && mainPhoto.url.isNotEmpty()) {
                    Img(
                        src = mainPhoto.url,
                        attrs = {
                            style {
                                width(100.percent)
                                height(400.px)
                                property("object-fit", "cover")
                                borderRadius(8.px)
                            }
                        },
                    )
                } else {
                    Div({
                        style {
                            width(100.percent)
                            height(400.px)
                            backgroundColor(CSSColors.Gray300)
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                            justifyContent(JustifyContent.Center)
                            borderRadius(8.px)
                        }
                    }) {
                        Text("Нет фото")
                    }
                }
            }

            Div({
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    gap(8.px)
                    width(200.px)
                }
            }) {
                thumbnailPhotos.forEach { photo ->
                    Div({
                        style {
                            position(Position.Relative)
                            cursor("pointer")
                        }
                    }) {
                        Img(
                            src = photo.url,
                            attrs = {
                                style {
                                    width(100.percent)
                                    height(120.px)
                                    property("object-fit", "cover")
                                    borderRadius(8.px)
                                }
                            },
                        )
                    }
                }

                if (allPhotos.size > 3) {
                    Button({
                        style {
                            width(100.percent)
                            padding(12.px)
                            borderRadius(8.px)
                            border(1.px, LineStyle.Solid, CSSColors.Gray300)
                            backgroundColor(CSSColors.White)
                            cursor("pointer")
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                            justifyContent(JustifyContent.Center)
                            gap(8.px)
                        }
                        onClick {
                            window.location.href = "/car-photos-gallery?id=${car.id}"
                        }
                    }) {
                        Span({
                            style {
                                fontSize(14.px)
                                color(CSSColors.Gray600)
                            }
                        }) {
                            Text("View ${allPhotos.size} photos")
                        }
                    }
                }
            }
        }

        Div({
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                gap(16.px)
            }
        }) {
            val carName = "${car.resolvedBrandName()} ${car.resolvedModelName()}".trim()
            val carYear = car.resolvedProductionYear()

            if (carName.isNotEmpty()) {
                Div({
                    style {
                        fontSize(32.px)
                        fontWeight("700")
                        color(CSSColors.Black)
                    }
                }) {
                    Text(carName)
                }
            }

            if (carYear > 0) {
                Div({
                    style {
                        fontSize(24.px)
                        fontWeight("600")
                        color(CSSColors.Gray600)
                    }
                }) {
                    Text("$carYear")
                }
            }

            val dailyRate = car.resolvedDailyRate()
            if (dailyRate > 0) {
                Div({
                    style {
                        fontSize(24.px)
                        fontWeight("600")
                        color(CSSColors.Black)
                        marginTop(16.px)
                    }
                }) {
                    Text("от ${dailyRate.toInt()} ₽ / сутки")
                }
            }

            Div({
                style {
                    display(DisplayStyle.Flex)
                    flexWrap(FlexWrap.Wrap)
                    gap(12.px)
                    marginTop(16.px)
                }
            }) {
                val seatsCount = car.resolvedSeatsCount()
                if (seatsCount > 0) {
                    CarSpecChip(
                        iconPath = "images/menu/user.svg",
                        text = "$seatsCount мест",
                    )
                }

                val engineType = car.resolvedEngineTypeTranslate()
                if (engineType.isNotEmpty()) {
                    CarSpecChip(
                        iconPath = "images/fuel-pump.svg",
                        text = engineType,
                    )
                }

                val engineVolume = car.resolvedEngineVolume()
                if (engineVolume > 0) {
                    CarSpecChip(
                        iconPath = "images/fuel.svg",
                        text = "${engineVolume.toInt()} л",
                    )
                }

                val transmissionType = car.chassis?.transmissionTranslate
                if (!transmissionType.isNullOrEmpty()) {
                    CarSpecChip(
                        iconPath = "images/back-wheel-drive.svg",
                        text = transmissionType,
                    )
                }
            }
        }
    }
}
