package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.ButtonViewModel
import my.drivebit.viewmodels.createButtonViewModel
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text

enum class ActionButtonVariant {
    /** Solid fill, white label — Master primary/secondary */
    Primary,

    /** White fill, gray-main label, subtle border — Master ghost */
    Ghost,
}

@Composable
fun ActionButton(
    image: String? = null,
    enabledColor: CSSColorValue = CSSColors.Blue,
    text: String,
    onClick: () -> Unit,
    viewModel: ButtonViewModel = createButtonViewModel(),
    variant: ActionButtonVariant = ActionButtonVariant.Primary,
) {
    val buttonState by viewModel.state.collectAsState()
    val isEnabled = buttonState is ButtonState.Enabled
    val isLoading = buttonState is ButtonState.Loading
    val isDisabled = buttonState is ButtonState.Disabled || isLoading

    val bgColor =
        when {
            isDisabled -> CSSColors.DisabledBg
            variant == ActionButtonVariant.Ghost -> CSSColors.White
            else -> enabledColor
        }
    val fgColor =
        when {
            isDisabled -> CSSColors.Gray5
            variant == ActionButtonVariant.Ghost -> CSSColors.Black
            else -> CSSColors.White
        }
    val hoverBg =
        when (variant) {
            ActionButtonVariant.Ghost -> CSSColors.Gray7String
            ActionButtonVariant.Primary -> CSSColors.BlueHoverString
        }
    val idleBg =
        when {
            isDisabled -> CSSColors.DisabledBgString
            variant == ActionButtonVariant.Ghost -> CSSColors.WhiteString
            else -> enabledColor.toString()
        }

    Button({
        onClick {
            if (isEnabled) {
                onClick()
            }
        }
        if (isDisabled) {
            disabled()
        }
        attr("aria-disabled", if (isDisabled) "true" else "false")
        style {
            width(100.percent)
            height(52.px)
            minHeight(52.px)
            padding(0.px, 24.px)
            borderRadius(16.px)
            property("box-sizing", "border-box")
            backgroundColor(bgColor)
            color(fgColor)
            if (variant == ActionButtonVariant.Ghost && !isDisabled) {
                border(1.px, LineStyle.Solid, CSSColors.BorderSubtle)
            } else {
                border(0.px)
            }
            cursor(if (isEnabled) "pointer" else "not-allowed")
            applyTypography(CSSTypography.Styles.button)
            fontSize(CSSTypography.FontSize.base)
            property("font-weight", "500")
            property("font-family", "Roboto, sans-serif")
            property("transition", "background-color 0.15s ease, border-color 0.15s ease, color 0.15s ease")
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.Center)
            gap(8.px)
        }
        onMouseEnter {
            if (isEnabled) {
                (it.target as org.w3c.dom.HTMLButtonElement).style.setProperty(
                    "background-color",
                    hoverBg,
                )
            }
        }
        onMouseLeave {
            if (isEnabled) {
                (it.target as org.w3c.dom.HTMLButtonElement).style.setProperty(
                    "background-color",
                    idleBg,
                )
            }
        }
    }) {
        if (isLoading) {
            Div({
                style {
                    width(16.px)
                    height(16.px)
                    border(2.px, LineStyle.Solid, fgColor)
                    property("border-top-color", "transparent")
                    borderRadius(50.percent)
                    property("animation", "spin 1s linear infinite")
                    property("display", "inline-block")
                }
            })
        }
        if (image != null && !isLoading) {
            Img(
                src = image,
                alt = "",
                attrs = {
                    style {
                        width(20.px)
                        height(20.px)
                        property("object-fit", "contain")
                        if (variant == ActionButtonVariant.Primary && !isDisabled) {
                            property("filter", "brightness(0) invert(1)")
                        }
                    }
                },
            )
        }
        Text(if (isLoading) "Загрузка..." else text)
    }
}
