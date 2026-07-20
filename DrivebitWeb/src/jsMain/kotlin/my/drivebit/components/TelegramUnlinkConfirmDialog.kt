package my.drivebit.components

import androidx.compose.runtime.Composable
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun TelegramUnlinkConfirmDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Div({
        style {
            position(Position.Fixed)
            top(0.px)
            left(0.px)
            right(0.px)
            bottom(0.px)
            property("background-color", "rgba(0, 0, 0, 0.5)")
            property("z-index", "1000")
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.Center)
            padding(16.px)
        }
        onClick { onCancel() }
    }) {
        Div({
            style {
                width(100.percent)
                property("max-width", "360px")
                backgroundColor(CSSColors.White)
                borderRadius(12.px)
                padding(18.px)
                border(1.px, LineStyle.Solid, CSSColors.Gray300)
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                gap(16.px)
                property("box-shadow", "0 8px 24px rgba(0, 0, 0, 0.2)")
                property("margin", "0 12px")
            }
            onClick { it.stopPropagation() }
        }) {
            Span({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    fontSize(CSSTypography.FontSize.base)
                    color(CSSColors.Black)
                    lineHeight("1.45")
                }
            }) {
                Text("Вы перестанете получать уведомления в телеграм")
            }

            Div({
                style {
                    display(DisplayStyle.Flex)
                    justifyContent(JustifyContent.FlexEnd)
                    gap(8.px)
                }
            }) {
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
                    }
                    onClick { onCancel() }
                }) {
                    Text("Отмена")
                }

                Div({
                    style {
                        padding(10.px, 18.px)
                        borderRadius(8.px)
                        backgroundColor(CSSColors.Black)
                        color(CSSColors.White)
                        cursor("pointer")
                        applyTypography(CSSTypography.Styles.button)
                        fontSize(CSSTypography.FontSize.sm)
                        fontWeight(CSSTypography.FontWeight.semibold)
                    }
                    onClick { onConfirm() }
                }) {
                    Text("Ок")
                }
            }
        }
    }
}
