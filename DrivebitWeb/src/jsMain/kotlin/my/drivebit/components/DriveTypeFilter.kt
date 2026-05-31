package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.viewmodels.DriveTypeViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

@Composable
fun DriveTypeFilter(
    onDriveTypeSelected: (name: String, translate: String) -> Unit,
    onReset: () -> Unit,
) {
    val driveTypeViewModel: DriveTypeViewModel = koinInject()
    val driveTypes by driveTypeViewModel.driveTypes.collectAsState()
    val error by driveTypeViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        driveTypeViewModel.loadDriveTypes()
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
        Span({
            style {
                applyTypography(CSSTypography.Styles.body)
                fontSize(CSSTypography.FontSize.base)
                fontWeight(CSSTypography.FontWeight.semibold)
                color(CSSColors.Black)
            }
        }) {
            Text("Выберите привод")
        }

        if (error != null) {
            TextError(error ?: "Произошла ошибка")
        } else {
            StringList(
                strings = driveTypes.map { it.translate },
                onSelected = { translate ->
                    val driveType = driveTypes.find { it.translate == translate }
                    driveType?.let {
                        onDriveTypeSelected(it.name, it.translate)
                    }
                },
            )
        }

        FilterResetButton(onReset = onReset)
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
        Text("Сбросить")
    }
}
