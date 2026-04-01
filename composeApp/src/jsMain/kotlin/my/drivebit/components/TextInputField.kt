package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import my.drivebit.components.Column
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLInputElement

private const val PASSWORD_TOGGLE_EYE_OPEN =
    "url(\"data:image/svg+xml,%3Csvg%20xmlns%3D%27http%3A//www.w3.org/2000/svg%27%20width%3D%2720%27%20height%3D%2720%27%20viewBox%3D%270%200%2024%2024%27%20fill%3D%27none%27%20stroke%3D%27%23666%27%20stroke-width%3D%272%27%20stroke-linecap%3D%27round%27%20stroke-linejoin%3D%27round%27%3E%3Cpath%20d%3D%27M1%2012s4-8%2011-8%2011%208%2011%208-4%208-11%208-11-8-11-8z%27/%3E%3Ccircle%20cx%3D%2712%27%20cy%3D%2712%27%20r%3D%273%27/%3E%3C/svg%3E\")"

private const val PASSWORD_TOGGLE_EYE_OFF =
    "url(\"data:image/svg+xml,%3Csvg%20xmlns%3D%27http%3A//www.w3.org/2000/svg%27%20width%3D%2720%27%20height%3D%2720%27%20viewBox%3D%270%200%2024%2024%27%20fill%3D%27none%27%20stroke%3D%27%23666%27%20stroke-width%3D%272%27%20stroke-linecap%3D%27round%27%20stroke-linejoin%3D%27round%27%3E%3Cpath%20d%3D%27M17.94%2017.94A10.07%2010.07%200%200%201%2012%2020c-7%200-11-8-11-8a18.45%2018.45%200%200%201%205.06-5.94M9.9%204.24A9.12%209.12%200%200%201%2012%204c7%200%2011%208%2011%208a18.5%2018.5%200%200%201-2.16%203.19m-6.72-1.07a3%203%200%201%201-4.24-4.24%27/%3E%3Cline%20x1%3D%271%27%20y1%3D%271%27%20x2%3D%2723%27%20y2%3D%2723%27/%3E%3C/svg%3E\")"

@Composable
fun TextInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    inputType: InputType<String> = InputType.Text,
    fitContainerWidth: Boolean = false,
    placeholder: String? = null,
    maxLength: Int? = null,
    onFocus: (() -> Unit)? = null,
    onBlur: (() -> Unit)? = null,
    numeric: Boolean = false,
    errorMessage: String? = null,
    passwordVisibilityToggle: Boolean = false,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val resolvedInputType =
        when {
            passwordVisibilityToggle && passwordVisible -> InputType.Text
            passwordVisibilityToggle -> InputType.Password
            else -> inputType
        }

    Column(gap = 12.px) {
        Span({
            style {
                applyTypography(CSSTypography.Styles.body)
                fontSize(CSSTypography.FontSize.sm)
                fontWeight(CSSTypography.FontWeight.medium)
                color(CSSColors.Black)
            }
        }) {
            Text(label)
        }

        if (passwordVisibilityToggle) {
            Div({
                style {
                    position(Position.Relative)
                    width(100.percent)
                    if (fitContainerWidth) {
                        property("box-sizing", "border-box")
                    }
                }
            }) {
                Input(
                    type = resolvedInputType,
                    attrs = {
                        value(value)
                        placeholder?.let { attr("placeholder", it) }
                        maxLength?.let { attr("maxlength", it.toString()) }
                        if (numeric) {
                            attr("inputmode", "numeric")
                            attr("pattern", "[0-9]*")
                        }
                        onInput { event ->
                            val newValue = (event.target as HTMLInputElement).value
                            if (numeric) {
                                val digitsOnly = newValue.filter { it.isDigit() }
                                onValueChange(digitsOnly)
                            } else {
                                onValueChange(newValue)
                            }
                        }
                        onFocus?.let { handler ->
                            onFocus { handler() }
                        }
                        onBlur?.let { handler ->
                            onBlur { handler() }
                        }
                        style {
                            applyTypography(CSSTypography.Styles.body)
                            fontSize(CSSTypography.FontSize.base)
                            color(CSSColors.Black)
                            val borderColor = if (errorMessage != null) CSSColors.Red else CSSColors.Gray600
                            border(1.px, LineStyle.Solid, borderColor)
                            borderRadius(8.px)
                            padding(12.px, 16.px)
                            paddingRight(48.px)
                            width(100.percent)
                            if (fitContainerWidth) {
                                property("box-sizing", "border-box")
                            }
                        }
                    },
                )
                Button(
                    {
                        attr("type", "button")
                        attr(
                            "aria-label",
                            if (passwordVisible) "Скрыть пароль" else "Показать пароль",
                        )
                        onClick { passwordVisible = !passwordVisible }
                        style {
                            position(Position.Absolute)
                            right(4.px)
                            property("top", "50%")
                            property("transform", "translateY(-50%)")
                            width(40.px)
                            height(40.px)
                            padding(0.px)
                            border(0.px)
                            borderRadius(6.px)
                            backgroundColor(rgba(0, 0, 0, 0))
                            cursor("pointer")
                            property(
                                "background-image",
                                if (passwordVisible) PASSWORD_TOGGLE_EYE_OFF else PASSWORD_TOGGLE_EYE_OPEN,
                            )
                            property("background-repeat", "no-repeat")
                            property("background-position", "center")
                            property("background-size", "20px 20px")
                        }
                    },
                ) {}
            }
        } else {
            Input(
                type = resolvedInputType,
                attrs = {
                    value(value)
                    placeholder?.let { attr("placeholder", it) }
                    maxLength?.let { attr("maxlength", it.toString()) }
                    if (numeric) {
                        attr("inputmode", "numeric")
                        attr("pattern", "[0-9]*")
                    }
                    onInput { event ->
                        val newValue = (event.target as HTMLInputElement).value
                        if (numeric) {
                            val digitsOnly = newValue.filter { it.isDigit() }
                            onValueChange(digitsOnly)
                        } else {
                            onValueChange(newValue)
                        }
                    }
                    onFocus?.let { handler ->
                        onFocus { handler() }
                    }
                    onBlur?.let { handler ->
                        onBlur { handler() }
                    }
                    style {
                        applyTypography(CSSTypography.Styles.body)
                        fontSize(CSSTypography.FontSize.base)
                        color(CSSColors.Black)
                        val borderColor = if (errorMessage != null) CSSColors.Red else CSSColors.Gray600
                        border(1.px, LineStyle.Solid, borderColor)
                        borderRadius(8.px)
                        padding(12.px, 16.px)
                        width(100.percent)
                        if (fitContainerWidth) {
                            property("box-sizing", "border-box")
                        }
                    }
                },
            )
        }

        if (errorMessage != null) {
            Span({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    fontSize(CSSTypography.FontSize.sm)
                    color(CSSColors.Red)
                }
            }) {
                Text(errorMessage)
            }
        }
    }
}
