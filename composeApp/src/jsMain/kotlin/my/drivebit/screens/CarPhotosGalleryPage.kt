package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import my.drivebit.components.AppWithHeader
import my.drivebit.components.Column
import my.drivebit.components.Loader
import my.drivebit.components.TextError
import my.drivebit.design.CSSColors
import my.drivebit.utils.getUrlParameter
import my.drivebit.viewmodels.CarDetailState
import my.drivebit.viewmodels.CarDetailViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.currentKoinScope
import org.koin.core.parameter.parametersOf

@Composable
fun CarPhotosGalleryPage() {
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
                    CarPhotosGalleryContent(car = currentState.car)
                }
            }
        }
    }
}

@Composable
private fun CarPhotosGalleryContent(car: my.drivebit.network.services.CarDetailResponse) {
    val allPhotos = (car.photos + (car.general?.photos ?: emptyList())).distinctBy { it.id }

    Column(gap = 24.px) {
        Div({
            style {
                fontSize(24.px)
                fontWeight("700")
                color(CSSColors.Black)
            }
        }) {
            Text("Фотографии")
        }

        if (allPhotos.isEmpty()) {
            Div({
                style {
                    padding(40.px)
                    textAlign("center")
                    color(CSSColors.Gray600)
                }
            }) {
                Text("Фотографии отсутствуют")
            }
        } else {
            Column(
                gap = 16.px,
                modifier = { width(100.percent) },
            ) {
                allPhotos.forEach { photo ->
                    if (photo.url.isNotEmpty()) {
                        Div({
                            style {
                                width(100.percent)
                                borderRadius(8.px)
                                overflow("hidden")
                                backgroundColor(CSSColors.White)
                            }
                        }) {
                            Img(
                                src = photo.url,
                                attrs = {
                                    style {
                                        width(100.percent)
                                        property("height", "auto")
                                        display(DisplayStyle.Block)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
