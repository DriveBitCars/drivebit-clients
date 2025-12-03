package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.browser.document
import my.drivebit.components.AppContainer
import my.drivebit.components.Logo
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.viewmodels.ProfileState
import my.drivebit.viewmodels.ProfileViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

@Composable
fun ProfilePage(viewModel: ProfileViewModel = koinInject()) {
    val navigationController = LocalNavigationController.current
    val state by viewModel.state.collectAsState()

    SideEffect {
        val existingStyle = document.getElementById("profile-spinner-style")
        if (existingStyle == null) {
            val style = document.createElement("style") as org.w3c.dom.HTMLStyleElement
            style.id = "profile-spinner-style"
            style.textContent =
                "@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }"
            document.head?.appendChild(style)
        }
    }

    AppContainer {
        Div({
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                marginBottom(20.px)
            }
        }) {
            Logo()

            Button({
                onClick { navigationController?.navigateTo("/") }
                style {
                    applyTypography(CSSTypography.Styles.button)
                    fontSize(CSSTypography.FontSize.base)
                    backgroundColor(Color.transparent)
                    border(0.px, LineStyle.None, Color.transparent)
                    cursor("pointer")
                    color(CSSColors.BlueRed)
                }
            }) {
                Text("Назад")
            }
        }

        Div({
            style {
                padding(24.px)
                maxWidth(800.px)
                margin(0.px)
                property("margin-left", "auto")
                property("margin-right", "auto")
            }
        }) {
            when (val currentState = state) {
                is ProfileState.Loading -> {
                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            justifyContent(JustifyContent.Center)
                            alignItems(AlignItems.Center)
                            padding(48.px)
                        }
                    }) {
                        Div({
                            style {
                                width(48.px)
                                height(48.px)
                                border(3.px, LineStyle.Solid, CSSColors.Gray300)
                                property("border-top-color", CSSColors.BlueRedString)
                                borderRadius(50.percent)
                                property("animation", "spin 1s linear infinite")
                            }
                        }) {}
                    }
                }

                is ProfileState.Error -> {
                    Div({
                        style {
                            textAlign("center")
                            padding(24.px)
                        }
                    }) {
                        Span({
                            style {
                                applyTypography(CSSTypography.Styles.body)
                                fontSize(CSSTypography.FontSize.base)
                                color(CSSColors.Red)
                            }
                        }) {
                            Text("Ошибка: ${currentState.message}")
                        }

                        Button({
                            onClick { viewModel.loadProfile() }
                            style {
                                marginTop(16.px)
                                applyTypography(CSSTypography.Styles.button)
                                fontSize(CSSTypography.FontSize.base)
                                fontWeight(CSSTypography.FontWeight.semibold)
                                backgroundColor(CSSColors.BlueRed)
                                color(CSSColors.White)
                                border(0.px, LineStyle.None, Color.transparent)
                                borderRadius(8.px)
                                padding(12.px, 24.px)
                                cursor("pointer")
                            }
                        }) {
                            Text("Повторить")
                        }
                    }
                }

                is ProfileState.Success -> {
                    val user = currentState.user

                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Column)
                            gap(16.px)
                        }
                    }) {
                        if (user.firstName != null || user.lastName != null) {
                            Span({
                                style {
                                    applyTypography(CSSTypography.Styles.body)
                                    fontSize(CSSTypography.FontSize.xxl)
                                    fontWeight(CSSTypography.FontWeight.bold)
                                    color(CSSColors.Black)
                                }
                            }) {
                                Text(
                                    buildString {
                                        user.firstName?.let { append(it) }
                                        user.middleName?.let { append(" $it") }
                                        user.lastName?.let { append(" $it") }
                                    }.trim(),
                                )
                            }
                        }

                        user.createdAt?.let { createdAt ->
                            Span({
                                style {
                                    applyTypography(CSSTypography.Styles.body)
                                    fontSize(CSSTypography.FontSize.base)
                                    color(CSSColors.Gray600)
                                }
                            }) {
                                Text("Присоединился: $createdAt")
                            }
                        }

                        Button({
                            onClick { }
                            style {
                                applyTypography(CSSTypography.Styles.button)
                                fontSize(CSSTypography.FontSize.base)
                                fontWeight(CSSTypography.FontWeight.semibold)
                                backgroundColor(CSSColors.BlueRed)
                                color(CSSColors.White)
                                border(0.px, LineStyle.None, Color.transparent)
                                borderRadius(8.px)
                                padding(12.px, 24.px)
                                cursor("pointer")
                                alignSelf(AlignSelf.FlexStart)
                            }
                        }) {
                            Text("Редактировать профиль")
                        }

                        Span({
                            style {
                                applyTypography(CSSTypography.Styles.body)
                                fontSize(CSSTypography.FontSize.xs)
                                fontWeight(CSSTypography.FontWeight.semibold)
                                color(CSSColors.Gray600)
                                marginTop(24.px)
                            }
                        }) {
                            Text("ПРОВЕРЕННАЯ ИНФОРМАЦИЯ")
                        }

                        user.phone?.let { phone ->
                            ProfileInfoRow(
                                label = "Номер телефона",
                                value = phone,
                                action = "Подтвердить номер телефона",
                            )
                        }

                        user.email?.let { email ->
                            ProfileInfoRow(
                                label = "Email адрес",
                                value = email,
                                action = "Подтверждено",
                            )
                        }

                        Span({
                            style {
                                applyTypography(CSSTypography.Styles.body)
                                fontSize(CSSTypography.FontSize.xs)
                                fontWeight(CSSTypography.FontWeight.semibold)
                                color(CSSColors.Gray600)
                                marginTop(24.px)
                            }
                        }) {
                            Text("ОТЗЫВЫ ОТ ХОСТОВ")
                        }

                        Span({
                            style {
                                applyTypography(CSSTypography.Styles.body)
                                fontSize(CSSTypography.FontSize.base)
                                color(CSSColors.Black)
                            }
                        }) {
                            Text("Пока нет отзывов")
                        }

                        Span({
                            style {
                                applyTypography(CSSTypography.Styles.body)
                                fontSize(CSSTypography.FontSize.sm)
                                color(CSSColors.Gray600)
                            }
                        }) {
                            Text("Пока не получено отзывов на Drivebit")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
    action: String,
) {
    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            gap(4.px)
        }
    }) {
        Span({
            style {
                applyTypography(CSSTypography.Styles.body)
                fontSize(CSSTypography.FontSize.base)
                color(CSSColors.Black)
            }
        }) {
            Text(label)
        }

        Span({
            style {
                applyTypography(CSSTypography.Styles.body)
                fontSize(CSSTypography.FontSize.sm)
                color(CSSColors.Gray600)
            }
        }) {
            Text(value)
        }

        Span({
            style {
                applyTypography(CSSTypography.Styles.body)
                fontSize(CSSTypography.FontSize.sm)
                color(CSSColors.BlueRed)
            }
        }) {
            Text(action)
        }
    }
}
