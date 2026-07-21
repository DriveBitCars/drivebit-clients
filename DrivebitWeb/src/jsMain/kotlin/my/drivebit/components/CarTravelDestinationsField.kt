package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.repositories.EnumItem
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun CarTravelDestinationsField(
    options: List<EnumItem>,
    selectedNames: List<String>,
    onToggle: (String) -> Unit,
) {
    Column(gap = 12.px) {
        Span({
            style {
                applyTypography(CSSTypography.Styles.body)
                fontSize(CSSTypography.FontSize.sm)
                fontWeight(CSSTypography.FontWeight.medium)
                color(CSSColors.Black)
            }
        }) {
            Text("Направления путешествий")
        }
        Div({
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                gap(8.px)
            }
        }) {
            options.forEach { option ->
                val checkboxId = "travel-destination-${option.name}"
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        gap(8.px)
                    }
                }) {
                    Input(
                        type = InputType.Checkbox,
                        attrs = {
                            id(checkboxId)
                            checked(option.name in selectedNames)
                            onInput {
                                onToggle(option.name)
                            }
                            style {
                                width(20.px)
                                height(20.px)
                                cursor("pointer")
                                flexShrink(0)
                            }
                        },
                    )
                    Label(
                        forId = checkboxId,
                        attrs = {
                            style {
                                cursor("pointer")
                                applyTypography(CSSTypography.Styles.body)
                                color(CSSColors.Black)
                            }
                        },
                    ) {
                        Text(option.translate)
                    }
                }
            }
        }
    }
}
