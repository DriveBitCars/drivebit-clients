package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.browser.window
import my.drivebit.components.ActionButton
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.FormSection
import my.drivebit.components.Loader
import my.drivebit.components.PageHeader
import my.drivebit.components.PageWithLogo
import my.drivebit.components.TextError
import my.drivebit.components.TextSmartHeader
import my.drivebit.components.UniversalButton
import my.drivebit.design.CSSColors
import my.drivebit.viewmodels.MyCarsViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

@Composable
fun MyCarsPage() {
    val viewModel: MyCarsViewModel = koinInject()

    val cars by viewModel.cars.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCars()
    }

    PageWithLogo {
        CenteredFormContainer {
            PageHeader {
                TextSmartHeader("Мои авто")
            }

            FormSection {
                when {
                    isLoading -> {
                        Loader()
                    }

                    error != null -> {
                        val errorMessage = error ?: "Произошла ошибка"
                        TextError(errorMessage)
                        if (errorMessage.contains("авторизац", ignoreCase = true) ||
                            errorMessage.contains(
                                "войдите",
                                ignoreCase = true,
                            )
                        ) {
                            Div({
                                style {
                                    textAlign("center")
                                    padding(16.px, 0.px)
                                    color(CSSColors.Gray600)
                                    fontSize(14.px)
                                }
                            }) {
                                Text("Перейдите в меню и войдите в систему")
                            }
                        }
                    }

                    cars.isEmpty() -> {
                        Div({
                            style {
                                textAlign("center")
                                padding(32.px)
                                color(CSSColors.Gray600)
                            }
                        }) {
                            Text("У вас пока нет автомобилей")
                        }
                    }

                    else -> {
                        Div({
                            style {
                                display(DisplayStyle.Grid)
                                gridTemplateColumns("repeat(auto-fill, minmax(200px, 1fr))")
                                gap(16.px)
                            }
                        }) {
                            cars.forEach { car ->
                                Div({
                                    style {
                                        display(DisplayStyle.Flex)
                                        flexDirection(FlexDirection.Column)
                                        borderRadius(8.px)
                                        property("border", "1px solid ${CSSColors.Gray300String}")
                                        property("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)")
                                        overflow("hidden")
                                        backgroundColor(CSSColors.White)
                                    }
                                }) {
                                    if (car.photos.isNotEmpty()) {
                                        Img(
                                            src = car.photos.first(),
                                            attrs = {
                                                style {
                                                    width(100.percent)
                                                    height(150.px)
                                                    property("object-fit", "cover")
                                                }
                                            },
                                        )
                                    } else {
                                        Div({
                                            style {
                                                width(100.percent)
                                                height(150.px)
                                                backgroundColor(CSSColors.Gray300)
                                                display(DisplayStyle.Flex)
                                                alignItems(AlignItems.Center)
                                                justifyContent(JustifyContent.Center)
                                                color(CSSColors.Gray600)
                                                fontSize(14.px)
                                            }
                                        }) {
                                            Text("Нет фото")
                                        }
                                    }
                                    Div({
                                        style {
                                            padding(12.px)
                                            display(DisplayStyle.Flex)
                                            flexDirection(FlexDirection.Column)
                                            gap(4.px)
                                        }
                                    }) {
                                        val carName =
                                            "${car.general?.brandName ?: ""} ${car.general?.modelName ?: ""}"
                                                .trim()
                                                .ifBlank { "Автомобиль #${car.id.take(8)}" }

                                        if (carName.isNotEmpty()) {
                                            Div({
                                                style {
                                                    fontSize(16.px)
                                                    fontWeight("600")
                                                    color(CSSColors.Black)
                                                }
                                            }) {
                                                Text(carName)
                                            }
                                        }

                                        car.general?.year?.let { year ->
                                            if (year > 0) {
                                                Div({
                                                    style {
                                                        fontSize(14.px)
                                                        color(CSSColors.Gray600)
                                                    }
                                                }) {
                                                    Text("$year год")
                                                }
                                            }
                                        }

                                        Div({
                                            style {
                                                marginTop(8.px)
                                            }
                                        }) {
                                            UniversalButton(
                                                isSelected = false,
                                                onClick = {
                                                    window.location.href = "/car-edit?carId=${car.id}"
                                                },
                                            ) {
                                                Span({
                                                    style {
                                                        fontSize(14.px)
                                                        fontWeight("500")
                                                    }
                                                }) {
                                                    Text("Управлять")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Div({
                style {
                    display(DisplayStyle.Flex)
                    justifyContent(JustifyContent.Center)
                    marginTop(24.px)
                }
            }) {
                ActionButton(
                    enabledColor = CSSColors.Blue,
                    text = "Добавить авто",
                    onClick = {
                        window.location.href = "http://192.168.0.100:8080/city-selection"
                    },
                )
            }
        }
    }
}
