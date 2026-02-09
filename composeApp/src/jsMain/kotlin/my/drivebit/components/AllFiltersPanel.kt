package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.viewmodels.ColorViewModel
import my.drivebit.viewmodels.EngineTypeViewModel
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.name
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

@Composable
fun AllFiltersPanel(
    selectedEngineTypeTranslate: String?,
    selectedColorTranslate: String?,
    selectedYearMin: Int?,
    selectedYearMax: Int?,
    selectedSeatsMax: Int?,
    selectedMileageMin: Int?,
    onEngineTypeSelected: (name: String, translate: String) -> Unit,
    onColorSelected: (name: String, translate: String) -> Unit,
    onYearMinChanged: (Int?) -> Unit,
    onYearMaxChanged: (Int?) -> Unit,
    onSeatsMaxChanged: (Int?) -> Unit,
    onMileageMinChanged: (Int?) -> Unit,
    onReset: () -> Unit,
) {
    val engineTypeViewModel: EngineTypeViewModel = koinInject()
    val colorViewModel: ColorViewModel = koinInject()
    val engineTypes by engineTypeViewModel.engineTypes.collectAsState()
    val colors by colorViewModel.colors.collectAsState()
    val engineTypeError by engineTypeViewModel.error.collectAsState()
    val colorError by colorViewModel.error.collectAsState()

    var yearMinInput by remember { mutableStateOf(selectedYearMin?.toString() ?: "") }
    var yearMaxInput by remember { mutableStateOf(selectedYearMax?.toString() ?: "") }
    var mileageMinInput by remember { mutableStateOf(selectedMileageMin?.toString() ?: "") }

    LaunchedEffect(selectedYearMin) {
        yearMinInput = selectedYearMin?.toString() ?: ""
    }

    LaunchedEffect(selectedYearMax) {
        yearMaxInput = selectedYearMax?.toString() ?: ""
    }

    LaunchedEffect(selectedMileageMin) {
        mileageMinInput = selectedMileageMin?.toString() ?: ""
    }

    LaunchedEffect(Unit) {
        engineTypeViewModel.loadEngineTypes()
        colorViewModel.loadColors()
    }

    Column(
        gap = 24.px,
        modifier = {
            padding(24.px)
            backgroundColor(CSSColors.White)
            borderRadius(12.px)
            property("box-shadow", "0 4px 12px rgba(0, 0, 0, 0.1)")
            width(400.px)
            property("max-width", "90vw")
            maxHeight(70.vh)
            property("overflow-y", "auto")
        },
    ) {
        FilterSection(
            title = "Тип двигателя",
            error = engineTypeError,
        ) {
            if (engineTypeError == null) {
                StringList(
                    strings = engineTypes.map { it.translate },
                    onSelected = { translate ->
                        val engineType = engineTypes.find { it.translate == translate }
                        engineType?.let {
                            onEngineTypeSelected(it.name, it.translate)
                        }
                    },
                )
            }
        }

        FilterSection(
            title = "Цвет",
            error = colorError,
        ) {
            if (colorError == null) {
                StringList(
                    strings = colors.map { it.translate },
                    onSelected = { translate ->
                        val color = colors.find { it.translate == translate }
                        color?.let {
                            onColorSelected(it.name, it.translate)
                        }
                    },
                )
            }
        }

        FilterSection(title = "Год выпуска") {
            Row(gap = 12.px) {
                NumberInputField(
                    label = "От",
                    value = yearMinInput,
                    onValueChange = { newValue ->
                        yearMinInput = newValue
                        onYearMinChanged(newValue.toIntOrNull())
                    },
                )
                NumberInputField(
                    label = "До",
                    value = yearMaxInput,
                    onValueChange = { newValue ->
                        yearMaxInput = newValue
                        onYearMaxChanged(newValue.toIntOrNull())
                    },
                )
            }
        }

        FilterSection(title = "Макс. кол-во мест") {
            Column(gap = 12.px) {
                SeatsMaxRadioOption(
                    value = 4,
                    label = "4 или менее",
                    selectedValue = selectedSeatsMax,
                    onSelected = { onSeatsMaxChanged(it) },
                )
                SeatsMaxRadioOption(
                    value = 5,
                    label = "5 или менее",
                    selectedValue = selectedSeatsMax,
                    onSelected = { onSeatsMaxChanged(it) },
                )
                SeatsMaxRadioOption(
                    value = 6,
                    label = "6 или менее",
                    selectedValue = selectedSeatsMax,
                    onSelected = { onSeatsMaxChanged(it) },
                )
                SeatsMaxRadioOption(
                    value = 7,
                    label = "7 или менее",
                    selectedValue = selectedSeatsMax,
                    onSelected = { onSeatsMaxChanged(it) },
                )
                SeatsMaxRadioOption(
                    value = 8,
                    label = "8 или менее",
                    selectedValue = selectedSeatsMax,
                    onSelected = { onSeatsMaxChanged(it) },
                )
            }
        }

        FilterSection(title = "Мин. пробег км/день") {
            NumberInputField(
                label = "Пробег",
                value = mileageMinInput,
                onValueChange = { newValue ->
                    mileageMinInput = newValue
                    onMileageMinChanged(newValue.toIntOrNull())
                },
            )
        }

        FilterResetButton(onReset = onReset)
    }
}

@Composable
private fun FilterSection(
    title: String,
    error: String? = null,
    content: @Composable () -> Unit,
) {
    Column(gap = 12.px) {
        Span({
            style {
                applyTypography(CSSTypography.Styles.body)
                fontSize(CSSTypography.FontSize.base)
                fontWeight(CSSTypography.FontWeight.semibold)
                color(CSSColors.Black)
            }
        }) {
            Text(title)
        }

        if (error != null) {
            TextError(error)
        } else {
            content()
        }
    }
}

@Composable
private fun NumberInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(gap = 8.px) {
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

        Input(
            type = InputType.Text,
            attrs = {
                value(value)
                attr("inputmode", "numeric")
                attr("pattern", "[0-9]*")
                onInput { event ->
                    val newValue = (event.target as org.w3c.dom.HTMLInputElement).value
                    val digitsOnly = newValue.filter { it.isDigit() }
                    onValueChange(digitsOnly)
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
}

@Composable
private fun SeatsMaxRadioOption(
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
                name("seatsMax")
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
        Text("Сбросить все")
    }
}
