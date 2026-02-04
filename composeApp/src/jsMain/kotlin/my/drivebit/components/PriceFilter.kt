package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import my.drivebit.design.CSSColors
import org.jetbrains.compose.web.css.*

@Composable
fun PriceFilter(
    minPrice: MutableState<Int> = remember { mutableStateOf(0) },
    maxPrice: MutableState<Int> = remember { mutableStateOf(600) },
    resultsCount: Int = 200,
    onReset: () -> Unit = {},
    onViewResults: () -> Unit = {},
) {
    val minValue by minPrice
    val maxValue by maxPrice
    var minInputValue by remember { mutableStateOf("$minValue") }
    var maxInputValue by remember { mutableStateOf("$maxValue+") }

    LaunchedEffect(minValue) {
        minInputValue = "$minValue"
    }

    LaunchedEffect(maxValue) {
        maxInputValue = "$maxValue+"
    }

    val sliderMax = 1000

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
        PriceFilterTitle()

        Row(
            gap = 12.px,
            alignItems = AlignItems.FlexStart,
        ) {
            PriceInputField(
                label = "Минимум",
                value = minInputValue,
                onValueChange = { newValue ->
                    minPrice.value = newValue
                    minInputValue = "$newValue"
                },
                minValue = 0,
                maxValue = maxValue,
            )

            PriceInputField(
                label = "Максимум",
                value = maxInputValue,
                onValueChange = { newValue ->
                    maxPrice.value = newValue
                    maxInputValue = "$newValue+"
                },
                minValue = minValue,
                maxValue = 10000,
            )
        }

        PriceRangeSlider(
            minPrice = minPrice,
            maxPrice = maxPrice,
            onMinValueChange = { minInputValue = it },
            onMaxValueChange = { maxInputValue = it },
            sliderMax = sliderMax,
        )

        PriceFilterButtons(
            minPrice = minPrice,
            maxPrice = maxPrice,
            onMinInputValueChange = { minInputValue = it },
            onMaxInputValueChange = { maxInputValue = it },
            resultsCount = resultsCount,
            onReset = onReset,
            onViewResults = onViewResults,
        )
    }
}
