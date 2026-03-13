package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import kotlinx.browser.window
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

private val PromoBlueBg: CSSColorValue = Color("#E3F2FD")
private val PromoChipBlue: CSSColorValue = Color("#4338CA")

@Composable
fun MainPromoSections() {
    Column(
        gap = 16.px,
        modifier = {
            width(100.percent)
            marginTop(20.px)
        },
    ) {
        WhySectionCard()
        EarnSectionCard()
    }
}

@Composable
private fun PromoCard(
    backgroundColor: CSSColorValue,
    content: @Composable () -> Unit,
) {
    Div({
        style {
            width(100.percent)
            padding(24.px, 32.px)
            backgroundColor(backgroundColor)
            borderRadius(12.px)
            property("box-shadow", "0 2px 12px rgba(0,0,0,0.06)")
            property("box-sizing", "border-box")
        }
    }) {
        content()
    }
}

@Composable
private fun WhySectionCard() {
    PromoCard(backgroundColor = PromoBlueBg) {
        Row(
            gap = 32.px,
            flexWrap = FlexWrap.Wrap,
            alignItems = AlignItems.Stretch,
            modifier = {
                width(100.percent)
            },
        ) {
            Column(
                gap = 6.px,
                modifier = {
                    flex(1)
                    property("min-height", "100%")
                    property("align-self", "stretch")
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    property("justify-content", "space-between")
                },
            ) {
                WhySectionHeading(
                    title = "Зачем вот это все?",
                    subtitle = "Преимущества аренды авто от частных владельцев",
                )
                Div({
                    style {
                        property("align-self", "flex-start")
                    }
                }) {
                    ActionButton(
                        enabledColor = CSSColors.Blue,
                        text = "Арендовать автомобиль",
                        onClick = { },
                    )
                }
            }
            AdvantageGrid()
        }
    }
}

@Composable
private fun WhySectionHeading(
    title: String,
    subtitle: String,
) {
    Div({
        style {
            flex(1)
            height(100.percent)
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            property("justify-content", "center")
            gap(100.px)
        }
    }) {
        Span({
            style {
                applyTypography(CSSTypography.Styles.body)
                fontSize(CSSTypography.FontSize.xxxl)
                fontWeight(CSSTypography.FontWeight.bold)
                color(CSSColors.Black)
                lineHeight("1.25")
            }
        }) {
            Text(title)
        }
        Span({
            style {
                applyTypography(CSSTypography.Styles.body)
                fontSize(CSSTypography.FontSize.xxl)
                color(CSSColors.Gray600)
            }
        }) {
            Text(subtitle)
        }
    }
}

private const val ADVANTAGE_TOOLTIP_TEXT =
    "Частники не зарабатывают на несоразмерной ответственности за повреждения и не штрафуют за ложные повреждения автомобиля."

@Composable
private fun AdvantageGrid() {
    Div({
        style {
            flex(2)
            property("min-width", "250px")
            display(DisplayStyle.Grid)
            property("grid-template-columns", "1fr 1fr")
            property("row-gap", "68px")
            property("column-gap", "40px")
        }
    }) {
        AdvantageChip("Стоит дешевле")
        AdvantageChip("Машины чище")
        AdvantageTooltipBlock(ADVANTAGE_TOOLTIP_TEXT)
        AdvantageChip("Пробег меньше")
        AdvantageChip("Выбор больше")
        AdvantageChip("Оформить проще")
    }
}

@Composable
private fun AdvantageTooltipBlock(bodyText: String) {
    Div({
        style {
            padding(14.px, 16.px)
            backgroundColor(CSSColors.White)
            borderRadius(8.px)
            property("box-shadow", "0 2px 8px rgba(0,0,0,0.08)")
            property("border", "1px solid rgba(0,0,0,0.08)")
        }
    }) {
        Column(
            modifier = {
                height(130.px)
                width(250.px) },
        ) {
            Span({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    fontSize(CSSTypography.FontSize.lg)
                    color(CSSColors.Black)
                    lineHeight("1.4")
                }
            }) {
                Text(bodyText)
            }
        }
    }
}

@Composable
private fun AdvantageChip(label: String) {
    Button({
        style {
            padding(14.px, 18.px)
            backgroundColor(PromoChipBlue)
            color(CSSColors.White)
            border(0.px)
            borderRadius(8.px)
            cursor("pointer")
            applyTypography(CSSTypography.Styles.button)
            fontSize(CSSTypography.FontSize.sm)
            fontWeight(CSSTypography.FontWeight.medium)
            property("text-align", "center")
        }
    }) {
        Text(label)
    }
}

@Composable
private fun EarnSectionCard() {
    PromoCard(backgroundColor = CSSColors.White) {
        Column(
            gap = 8.px,
            modifier = {
                width(100.percent)
                alignItems(AlignItems.Center)
            },
        ) {
            Span({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    fontSize(CSSTypography.FontSize.xxl)
                    fontWeight(CSSTypography.FontWeight.bold)
                    color(CSSColors.Black)
                    property("text-align", "center")
                    lineHeight("1.25")
                }
            }) {
                Text("Получайте доход от автомобиля с DriveBit")
            }
            Span({
                style {
                    applyTypography(CSSTypography.Styles.body)
                    fontSize(CSSTypography.FontSize.base)
                    color(CSSColors.Gray600)
                    property("text-align", "center")
                    property("max-width", "480px")
                    lineHeight("1.5")
                }
            }) {
                Text("Подайте заявку на регистрацию в сервисе и сдавайте свой автомобиль на время пока им не пользуетесь.")
            }
            Div({
                style {
                    marginTop(20.px)
                    width(100.percent)
                    property("max-width", "280px")
                }
            }) {
                ActionButton(
                    enabledColor = CSSColors.Blue,
                    text = "Сдать автомобиль",
                    onClick = { window.location.href = "/list-your-car" },
                )
            }
        }
    }
}
