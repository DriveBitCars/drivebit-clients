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

@Composable
fun ActionButton(
    image: String? = null,
    enabledColor: CSSColorValue,
    text: String,
    onClick: () -> Unit,
    viewModel: ButtonViewModel = createButtonViewModel(),
) {
    val buttonState by viewModel.state.collectAsState()
    val isEnabled = buttonState is ButtonState.Enabled
    val isLoading = buttonState is ButtonState.Loading
    val isDisabled = buttonState is ButtonState.Disabled

    Button({
        onClick {
            if (isEnabled) {
                onClick()
            }
        }
        if (isDisabled || isLoading) {
            disabled()
        }
        style {
            width(100.percent)
            padding(14.px, 24.px)
            borderRadius(8.px)
            property("box-sizing", "border-box")
            backgroundColor(
                if (isEnabled) {
                    enabledColor
                } else {
                    CSSColors.Gray300
                },
            )
            color(CSSColors.White)
            border(0.px)
            cursor(if (isEnabled) "pointer" else "not-allowed")
            applyTypography(CSSTypography.Styles.button)
            fontSize(CSSTypography.FontSize.base)
            fontWeight(CSSTypography.FontWeight.semibold)
            property("transition", "background-color 0.2s ease")
            property("opacity", if (isEnabled) "1" else "0.5")
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.Center)
            gap(8.px)
        }
        onMouseEnter {
            if (isEnabled) {
                (it.target as org.w3c.dom.HTMLButtonElement).style.setProperty(
                    "background-color",
                    CSSColors.BlueRedString,
                )
            }
        }
        onMouseLeave {
            if (isEnabled) {
                (it.target as org.w3c.dom.HTMLButtonElement).style.setProperty(
                    "background-color",
                    enabledColor.toString(),
                )
            }
        }
    }) {
        if (isLoading) {
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
        if (image != null && !isLoading) {
            Img(
                src = image,
                alt = "",
                attrs = {
                    style {
                        width(20.px)
                        height(20.px)
                        property("object-fit", "contain")
                    }
                },
            )
        }
        Text(if (isLoading) "Загрузка..." else text)
    }
}
