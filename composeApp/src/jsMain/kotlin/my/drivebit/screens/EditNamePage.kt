package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import my.drivebit.components.AppContainer
import my.drivebit.components.ErrorText
import my.drivebit.components.Loader
import my.drivebit.components.Logo
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.utils.getUrlParameterFromPath
import my.drivebit.viewmodels.EditProfileState
import my.drivebit.viewmodels.EditProfileViewModel
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

@Composable
fun EditNamePage(
    currentPath: String = "/edit-name",
    viewModel: EditProfileViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()
    val firstName by viewModel.firstName.collectAsState()
    val lastName by viewModel.lastName.collectAsState()
    val middleName by viewModel.middleName.collectAsState()
    val navigationController = LocalNavigationController.current

    LaunchedEffect(currentPath) {
        val initialFirstName = getUrlParameterFromPath(currentPath, "firstName")
        val initialLastName = getUrlParameterFromPath(currentPath, "lastName")
        val initialMiddleName = getUrlParameterFromPath(currentPath, "middleName")

        if (firstName.isEmpty() && lastName.isEmpty() && middleName.isEmpty()) {
            if (initialFirstName.isNotEmpty()) {
                viewModel.updateFirstName(initialFirstName)
            }
            if (initialLastName.isNotEmpty()) {
                viewModel.updateLastName(initialLastName)
            }
            if (initialMiddleName.isNotEmpty()) {
                viewModel.updateMiddleName(initialMiddleName)
            }
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
                is EditProfileState.Loading -> {
                    Loader()
                }

                is EditProfileState.Error -> {
                    Div({
                        style {
                            textAlign("center")
                            padding(24.px)
                        }
                    }) {
                        ErrorText("Ошибка: ${currentState.message}")
                    }
                }

                is EditProfileState.Success -> {
                    navigationController?.navigateTo("/profile")
                }

                is EditProfileState.Initial -> {
                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Column)
                            gap(24.px)
                            maxWidth(400.px)
                        }
                    }) {
                        Span({
                            style {
                                applyTypography(CSSTypography.Styles.body)
                                fontSize(CSSTypography.FontSize.xxl)
                                fontWeight(CSSTypography.FontWeight.bold)
                                color(CSSColors.Black)
                            }
                        }) {
                            Text("Редактирование профиля")
                        }

                        Div({
                            style {
                                display(DisplayStyle.Flex)
                                flexDirection(FlexDirection.Column)
                                gap(12.px)
                            }
                        }) {
                            Span({
                                style {
                                    applyTypography(CSSTypography.Styles.body)
                                    fontSize(CSSTypography.FontSize.sm)
                                    fontWeight(CSSTypography.FontWeight.medium)
                                    color(CSSColors.Black)
                                }
                            }) {
                                Text("Имя")
                            }

                            Input(
                                type = InputType.Text,
                                attrs = {
                                    value(firstName)
                                    onInput { event ->
                                        viewModel.updateFirstName((event.target as org.w3c.dom.HTMLInputElement).value)
                                    }
                                    style {
                                        applyTypography(CSSTypography.Styles.body)
                                        fontSize(CSSTypography.FontSize.base)
                                        color(CSSColors.Black)
                                        border(1.px, LineStyle.Solid, CSSColors.Gray600)
                                        borderRadius(8.px)
                                        padding(12.px, 16.px)
                                        width(100.percent)
                                    }
                                },
                            )
                        }

                        Div({
                            style {
                                display(DisplayStyle.Flex)
                                flexDirection(FlexDirection.Column)
                                gap(12.px)
                            }
                        }) {
                            Span({
                                style {
                                    applyTypography(CSSTypography.Styles.body)
                                    fontSize(CSSTypography.FontSize.sm)
                                    fontWeight(CSSTypography.FontWeight.medium)
                                    color(CSSColors.Black)
                                }
                            }) {
                                Text("Фамилия")
                            }

                            Input(
                                type = InputType.Text,
                                attrs = {
                                    value(lastName)
                                    onInput { event ->
                                        viewModel.updateLastName((event.target as org.w3c.dom.HTMLInputElement).value)
                                    }
                                    style {
                                        applyTypography(CSSTypography.Styles.body)
                                        fontSize(CSSTypography.FontSize.base)
                                        color(CSSColors.Black)
                                        border(1.px, LineStyle.Solid, CSSColors.Gray600)
                                        borderRadius(8.px)
                                        padding(12.px, 16.px)
                                        width(100.percent)
                                    }
                                },
                            )
                        }

                        Div({
                            style {
                                display(DisplayStyle.Flex)
                                flexDirection(FlexDirection.Column)
                                gap(12.px)
                            }
                        }) {
                            Span({
                                style {
                                    applyTypography(CSSTypography.Styles.body)
                                    fontSize(CSSTypography.FontSize.sm)
                                    fontWeight(CSSTypography.FontWeight.medium)
                                    color(CSSColors.Black)
                                }
                            }) {
                                Text("Отчество")
                            }

                            Input(
                                type = InputType.Text,
                                attrs = {
                                    value(middleName)
                                    onInput { event ->
                                        viewModel.updateMiddleName((event.target as org.w3c.dom.HTMLInputElement).value)
                                    }
                                    style {
                                        applyTypography(CSSTypography.Styles.body)
                                        fontSize(CSSTypography.FontSize.base)
                                        color(CSSColors.Black)
                                        border(1.px, LineStyle.Solid, CSSColors.Gray600)
                                        borderRadius(8.px)
                                        padding(12.px, 16.px)
                                        width(100.percent)
                                    }
                                },
                            )
                        }

                        Div({
                            style {
                                display(DisplayStyle.Flex)
                                flexDirection(FlexDirection.Row)
                                gap(12.px)
                            }
                        }) {
                            Button({
                                onClick {
                                    navigationController?.navigateTo("/profile")
                                }
                                style {
                                    applyTypography(CSSTypography.Styles.button)
                                    fontSize(CSSTypography.FontSize.base)
                                    fontWeight(CSSTypography.FontWeight.medium)
                                    backgroundColor(Color.transparent)
                                    color(CSSColors.Black)
                                    border(1.px, LineStyle.Solid, CSSColors.Gray600)
                                    borderRadius(8.px)
                                    padding(12.px, 24.px)
                                    cursor("pointer")
                                    flexGrow(1)
                                    property("box-sizing", "border-box")
                                }
                            }) {
                                Text("Отмена")
                            }

                            Button({
                                onClick {
                                    viewModel.save()
                                }
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
                                    flexGrow(1)
                                    property("box-sizing", "border-box")
                                }
                            }) {
                                Text("Сохранить")
                            }
                        }
                    }
                }
            }
        }
    }
}
