package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.name
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun SeatsFilter(
    selectedSeatsMin: Int?,
    resultsCount: Int,
    onSeatsMinSelected: (Int?) -> Unit,
    onReset: () -> Unit,
) {
    Column(
        gap = 16.px,
        modifier = {
            padding(24.px)
            backgroundColor(CSSColors.White)
            borderRadius(12.px)
            property("box-shadow", "0 4px 12px rgba(0, 0, 0, 0.1)")
            width(400.px)
            property("max-width", "90vw")
        },
    ) {
        Span({
            style {
                applyTypography(CSSTypography.Styles.body)
                fontSize(CSSTypography.FontSize.base)
                fontWeight(CSSTypography.FontWeight.semibold)
                color(CSSColors.Black)
            }
        }) {
            Text("Количество мест")
        }

        Column(gap = 12.px) {
            SeatsRadioOption(
                value = 4,
                label = "4 или более",
                selectedValue = selectedSeatsMin,
                onSelected = { onSeatsMinSelected(it) },
            )
            SeatsRadioOption(
                value = 5,
                label = "5 или более",
                selectedValue = selectedSeatsMin,
                onSelected = { onSeatsMinSelected(it) },
            )
            SeatsRadioOption(
                value = 6,
                label = "6 или более",
                selectedValue = selectedSeatsMin,
                onSelected = { onSeatsMinSelected(it) },
            )
            SeatsRadioOption(
                value = 7,
                label = "7 или более",
                selectedValue = selectedSeatsMin,
                onSelected = { onSeatsMinSelected(it) },
            )
            SeatsRadioOption(
                value = 8,
                label = "8 или более",
                selectedValue = selectedSeatsMin,
                onSelected = { onSeatsMinSelected(it) },
            )
        }

        Div({
            style {
                display(DisplayStyle.Flex)
                gap(8.px)
                marginTop(8.px)
            }
        }) {
            FilterResetButton(onReset = onReset)
        }
    }
}

@Composable
private fun SeatsRadioOption(
    value: Int,
    label: String,
    selectedValue: Int?,
    onSelected: (Int?) -> Unit,
) {
    val isSelected = selectedValue == value
    Div({
        style {
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            gap(12.px)
            cursor("pointer")
            padding(8.px, 0.px)
            property("transition", "background-color 0.2s ease")
        }
        onClick {
            if (isSelected) {
                onSelected(null)
            } else {
                onSelected(value)
            }
        }
    }) {
        Input(
            type = InputType.Radio,
            attrs = {
                name("seats")
                checked(isSelected)
                onClick { event ->
                    event.preventDefault()
                    if (isSelected) {
                        onSelected(null)
                    } else {
                        onSelected(value)
                    }
                }
                style {
                    width(20.px)
                    height(20.px)
                    cursor("pointer")
                }
            },
        )
        Span({
            style {
                applyTypography(CSSTypography.Styles.body)
                fontSize(CSSTypography.FontSize.base)
                color(CSSColors.Black)
                flex(1)
            }
        }) {
            Text(label)
        }
    }
}

@Composable
private fun FilterResetButton(onReset: () -> Unit) {
    Div({
        style {
            padding(10.px, 18.px)
            borderRadius(8.px)
            backgroundColor(CSSColors.Gray300)
            color(CSSColors.Black)
            cursor("pointer")
            applyTypography(CSSTypography.Styles.button)
            fontSize(CSSTypography.FontSize.sm)
            fontWeight(CSSTypography.FontWeight.medium)
            textAlign("center")
            property("transition", "background-color 0.2s ease")
            flex(1)
        }
        onClick { onReset() }
        onMouseEnter {
            (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                "background-color",
                CSSColors.Gray600String,
            )
        }
        onMouseLeave {
            (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                "background-color",
                CSSColors.Gray300String,
            )
        }
    }) {
        Text("Сбросить")
    }
}
