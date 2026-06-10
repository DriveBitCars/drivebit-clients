package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.browser.window
import my.drivebit.components.ActionButton
import my.drivebit.components.CarItemSmall
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.Column
import my.drivebit.components.FormSection
import my.drivebit.components.Loader
import my.drivebit.components.PageHeader
import my.drivebit.shell.PageWithLogo
import my.drivebit.components.Row
import my.drivebit.components.TextError
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.viewmodels.MyCarsViewModel
import my.drivebit.web.buildCarDetailUrl
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
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
                        Column(gap = 16.px) {
                            cars.forEach { car ->
                                Column(gap = 12.px) {
                                    CarItemSmall(
                                        car = car,
                                        href = buildCarDetailUrl(car.id),
                                    )
                                    Row(justifyContent = JustifyContent.Center) {
                                        ActionButton(
                                            enabledColor = CSSColors.Blue,
                                            text = "Управлять",
                                            onClick = {
                                                window.location.href = "/car-edit?id=${car.id}"
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Row(
                justifyContent = JustifyContent.Center,
                modifier = { marginTop(24.px) },
            ) {
                ActionButton(
                    enabledColor = CSSColors.Blue,
                    text = "Добавить авто",
                    onClick = {
                        window.location.href = "/address-input"
                    },
                )
            }
        }
    }
}
