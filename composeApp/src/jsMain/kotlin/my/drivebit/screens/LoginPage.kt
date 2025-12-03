package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import my.drivebit.components.AppContainer
import my.drivebit.components.Logo
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.resources.ImagePaths
import my.drivebit.viewmodels.AuthFormState
import my.drivebit.viewmodels.AuthFormViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

@Composable
fun LoginPage(
    viewModelQualifier: org.koin.core.qualifier.Qualifier,
    viewModel: AuthFormViewModel =
        koinInject(viewModelQualifier),
) {
    val initialValue = if (viewModel.fieldLabel == "Телефон") "+7" else ""
    var inputValue by remember { mutableStateOf(initialValue) }
    val navigationController = LocalNavigationController.current!!
    val loginState by viewModel.state.collectAsState()
    val validationState by viewModel.validationState.collectAsState()
    val isValid = validationState is my.drivebit.viewmodels.ValidationState.Valid
    val validationError =
        when (val state = validationState) {
            is my.drivebit.viewmodels.ValidationState.Error -> state.message
            else -> null
        }
    val isLoading = loginState is AuthFormState.Loading
    val isButtonDisabled = isLoading || !isValid

    if (loginState is AuthFormState.Success) {
        val identifier = (loginState as AuthFormState.Success).identifier
        val encodedIdentifier = js("encodeURIComponent")(identifier) as String
        navigationController.navigateTo("/verify-otp?identifier=$encodedIdentifier")
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
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.Center)
                minHeight(80.vh)
            }
        }) {
            Div({
                style {
                    width(100.percent)
                    maxWidth(400.px)
                }
            }) {
                Div({
                    style {
                        marginTop(0.px)
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
                        Text(viewModel.pageTitle)
                    }
                }

                Div({
                    style {
                        marginTop(32.px)
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                        gap(16.px)
                    }
                }) {
                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Column)
                            gap(8.px)
                        }
                    }) {
                        Span({
                            style {
                                applyTypography(CSSTypography.Styles.body)
                                fontSize(CSSTypography.FontSize.sm)
                                fontWeight(CSSTypography.FontWeight.medium)
                                color(CSSColors.Gray600)
                            }
                        }) {
                            Text(viewModel.fieldLabel)
                        }
                        Input(
                            type =
                                if (viewModel.fieldLabel == "Телефон") {
                                    org.jetbrains.compose.web.attributes.InputType.Tel
                                } else {
                                    org.jetbrains.compose.web.attributes.InputType.Email
                                },
                        ) {
                            value(inputValue)
                            onInput { event ->
                                val newValue = (event.target as org.w3c.dom.HTMLInputElement).value
                                inputValue = viewModel.formatInput(newValue)
                                viewModel.validateInput(inputValue)
                            }
                            style {
                                width(100.percent)
                                padding(12.px, 16.px)
                                borderRadius(8.px)
                                property("box-sizing", "border-box")
                                val hasError = loginState is AuthFormState.Error || validationError != null
                                val borderColor =
                                    if (hasError) {
                                        CSSColors.RedString
                                    } else {
                                        CSSColors.Gray300String
                                    }
                                property("border", "1px solid $borderColor")
                                property("font-size", "16px")
                                property("outline", "none")
                                property("transition", "border-color 0.2s ease")
                            }
                            onFocus {
                                val hasError = loginState is AuthFormState.Error || validationError != null
                                val borderColor =
                                    if (hasError) {
                                        CSSColors.RedString
                                    } else {
                                        CSSColors.BlueString
                                    }
                                (it.target as org.w3c.dom.HTMLInputElement).style.setProperty(
                                    "border-color",
                                    borderColor,
                                )
                            }
                            onBlur {
                                viewModel.validateInput(inputValue)
                                val hasError = loginState is AuthFormState.Error || validationError != null
                                val borderColor =
                                    if (hasError) {
                                        CSSColors.RedString
                                    } else {
                                        CSSColors.Gray300String
                                    }
                                (it.target as org.w3c.dom.HTMLInputElement).style.setProperty(
                                    "border-color",
                                    borderColor,
                                )
                            }
                            onKeyDown { event ->
                                val inputElement = event.target as org.w3c.dom.HTMLInputElement
                                if (event.key == "Backspace" &&
                                    inputElement.selectionStart == 0 &&
                                    inputElement.selectionEnd == 0
                                ) {
                                    event.preventDefault()
                                }
                            }
                        }
                        if (validationError != null) {
                            Span({
                                style {
                                    applyTypography(CSSTypography.Styles.body)
                                    fontSize(CSSTypography.FontSize.sm)
                                    color(CSSColors.Red)
                                }
                            }) {
                                Text(validationError)
                            }
                        } else if (loginState is AuthFormState.Error) {
                            Span({
                                style {
                                    applyTypography(CSSTypography.Styles.body)
                                    fontSize(CSSTypography.FontSize.sm)
                                    color(CSSColors.Red)
                                }
                            }) {
                                Text((loginState as AuthFormState.Error).message)
                            }
                        }
                    }
                }

                Button({
                    onClick {
                        viewModel.submit(inputValue)
                    }
                    style {
                        property("id", "primary-login-button")
                        width(100.percent)
                        marginTop(24.px)
                        padding(14.px, 24.px)
                        borderRadius(8.px)
                        property("box-sizing", "border-box")
                        val isLoading = loginState is AuthFormState.Loading
                        val isButtonEnabled = isValid && !isLoading
                        backgroundColor(if (isButtonEnabled) CSSColors.Blue else CSSColors.Gray300)
                        color(CSSColors.White)
                        border(0.px)
                        cursor(if (isButtonEnabled) "pointer" else "not-allowed")
                        applyTypography(CSSTypography.Styles.button)
                        fontSize(CSSTypography.FontSize.base)
                        fontWeight(CSSTypography.FontWeight.semibold)
                        property("transition", "background-color 0.2s ease")
                        property("opacity", if (isButtonEnabled) "1" else "0.6")
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        justifyContent(JustifyContent.Center)
                        gap(8.px)
                        property("disabled", if (isButtonDisabled) "true" else "false")
                    }
                    onMouseEnter {
                        if (loginState !is AuthFormState.Loading && isValid) {
                            (it.target as org.w3c.dom.HTMLButtonElement).style.setProperty(
                                "background-color",
                                CSSColors.BlueRedString,
                            )
                        }
                    }
                    onMouseLeave {
                        if (loginState !is AuthFormState.Loading && isValid) {
                            (it.target as org.w3c.dom.HTMLButtonElement).style.setProperty(
                                "background-color",
                                CSSColors.BlueString,
                            )
                        }
                    }
                }) {
                    if (loginState is AuthFormState.Loading) {
                        Div({
                            style {
                                width(16.px)
                                height(16.px)
                                border(2.px, LineStyle.Solid, CSSColors.White)
                                property("border-top-color", "transparent")
                                borderRadius(50.percent)
                                property("animation", "spin 1s linear infinite")
                                property("display", "inline-block")
                            }
                        })
                    }
                    Text(if (loginState is AuthFormState.Loading) "Загрузка..." else viewModel.primaryButtonText)
                }

                Div({
                    style {
                        marginTop(16.px)
                        textAlign("center")
                    }
                }) {
                    Span({
                        style {
                            applyTypography(CSSTypography.Styles.body)
                            fontSize(CSSTypography.FontSize.sm)
                            color(CSSColors.Gray600)
                        }
                    }) {
                        Text("Или")
                    }
                }

                Button({
                    onClick {
                        val path = viewModel.secondaryButtonNavigationPath
                        navigationController.navigateTo(path)
                    }
                    style {
                        width(100.percent)
                        marginTop(16.px)
                        padding(14.px, 24.px)
                        borderRadius(8.px)
                        property("box-sizing", "border-box")
                        backgroundColor(CSSColors.White)
                        color(CSSColors.Black)
                        property("border", "1px solid ${CSSColors.Gray300String}")
                        cursor("pointer")
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        justifyContent(JustifyContent.Center)
                        gap(8.px)
                        applyTypography(CSSTypography.Styles.button)
                        fontSize(CSSTypography.FontSize.base)
                        fontWeight(CSSTypography.FontWeight.semibold)
                        property("transition", "border-color 0.2s ease, background-color 0.2s ease")
                    }
                    onMouseEnter {
                        val button = it.target as org.w3c.dom.HTMLButtonElement
                        button.style.setProperty("border-color", CSSColors.Gray600String)
                        button.style.setProperty("background-color", CSSColors.Gray300String)
                    }
                    onMouseLeave {
                        val button = it.target as org.w3c.dom.HTMLButtonElement
                        button.style.setProperty("border-color", CSSColors.Gray300String)
                        button.style.setProperty("background-color", CSSColors.WhiteString)
                    }
                }) {
                    if (viewModel.fieldLabel == "Телефон") {
                        Img(
                            src = ImagePaths.LOGIN_LETTER_SVG,
                            alt = "Email icon",
                            attrs = {
                                style {
                                    width(20.px)
                                    height(20.px)
                                    property("object-fit", "contain")
                                }
                            },
                        )
                    }
                    Text(viewModel.secondaryButtonText)
                }
            }
        }
    }
}
