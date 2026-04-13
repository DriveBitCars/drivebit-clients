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
import my.drivebit.viewmodels.CarBrandViewModel
import my.drivebit.viewmodels.CarModelViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

@Composable
fun BrandModelFilter(
    onBrandSelected: (brandId: Int, brandName: String) -> Unit,
    onModelSelected: (modelId: Int, modelName: String) -> Unit,
    onReset: () -> Unit,
) {
    val brandViewModel: CarBrandViewModel = koinInject()
    val modelViewModel: CarModelViewModel = koinInject()
    val brands by brandViewModel.brands.collectAsState()
    val models by modelViewModel.models.collectAsState()
    val brandError by brandViewModel.error.collectAsState()
    val modelError by modelViewModel.error.collectAsState()

    var selectedBrandId by remember { mutableStateOf<Int?>(null) }
    var selectedBrandName by remember { mutableStateOf<String?>(null) }
    var brandQuery by remember { mutableStateOf("") }
    var modelQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        brandViewModel.loadBrands(existingInFleetOnly = true)
    }

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
        if (selectedBrandId == null) {
            BrandSelectionContent(
                brands = brands,
                error = brandError,
                query = brandQuery,
                onQueryChange = { newQuery ->
                    brandQuery = newQuery
                    brandViewModel.updateQuery(newQuery)
                },
                onBrandClick = { brand ->
                    selectedBrandId = brand.first
                    selectedBrandName = brand.second
                    brandQuery = ""
                    brandViewModel.clearQuery()
                    onBrandSelected(brand.first, brand.second)
                    modelViewModel.loadModels(brand.first, existingInFleetOnly = true)
                },
                onReset = onReset,
            )
        } else {
            ModelSelectionContent(
                brandName = selectedBrandName ?: "",
                models = models,
                error = modelError,
                query = modelQuery,
                onQueryChange = { newQuery ->
                    modelQuery = newQuery
                    modelViewModel.updateQuery(newQuery)
                },
                onModelClick = { model ->
                    onModelSelected(model.first, model.second)
                },
                onBack = {
                    selectedBrandId = null
                    selectedBrandName = null
                    modelQuery = ""
                    modelViewModel.clearQuery()
                },
                onReset = onReset,
            )
        }
    }
}

@Composable
private fun BrandSelectionContent(
    brands: List<my.drivebit.network.services.CarBrand>,
    error: String?,
    query: String,
    onQueryChange: (String) -> Unit,
    onBrandClick: (Pair<Int, String>) -> Unit,
    onReset: () -> Unit,
) {
    Span({
        style {
            applyTypography(CSSTypography.Styles.body)
            fontSize(CSSTypography.FontSize.base)
            fontWeight(CSSTypography.FontWeight.semibold)
            color(CSSColors.Black)
        }
    }) {
        Text("Выберите марку")
    }

    TextInputField(
        label = "Марка",
        value = query,
        placeholder = "Начните вводить...",
        onValueChange = onQueryChange,
    )

    if (error != null) {
        TextError(error)
    } else {
        StringList(
            strings = brands.map { it.name },
            onSelected = { brandName ->
                val brand = brands.find { it.name == brandName }
                brand?.let { onBrandClick(it.id to it.name) }
            },
        )
    }

    FilterResetButton(onReset = onReset)
}

@Composable
private fun ModelSelectionContent(
    brandName: String,
    models: List<my.drivebit.network.services.CarModel>,
    error: String?,
    query: String,
    onQueryChange: (String) -> Unit,
    onModelClick: (Pair<Int, String>) -> Unit,
    onBack: () -> Unit,
    onReset: () -> Unit,
) {
    Row(
        gap = 8.px,
        alignItems = AlignItems.Center,
    ) {
        Div({
            style {
                cursor("pointer")
                applyTypography(CSSTypography.Styles.body)
                fontSize(CSSTypography.FontSize.base)
                color(CSSColors.Blue)
            }
            onClick { onBack() }
        }) {
            Text("← $brandName")
        }
    }

    Span({
        style {
            applyTypography(CSSTypography.Styles.body)
            fontSize(CSSTypography.FontSize.base)
            fontWeight(CSSTypography.FontWeight.semibold)
            color(CSSColors.Black)
        }
    }) {
        Text("Выберите модель")
    }

    TextInputField(
        label = "Модель",
        value = query,
        placeholder = "Начните вводить...",
        onValueChange = onQueryChange,
    )

    if (error != null) {
        TextError(error)
    } else {
        StringList(
            strings = models.map { it.name },
            onSelected = { modelName ->
                val model = models.find { it.name == modelName }
                model?.let { onModelClick(it.id to it.name) }
            },
        )
    }

    FilterResetButton(onReset = onReset)
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
        Text("Сбросить")
    }
}
