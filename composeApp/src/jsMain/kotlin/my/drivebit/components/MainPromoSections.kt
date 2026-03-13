package my.drivebit.components

import androidx.compose.runtime.Composable
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
private val PromoIndigo: CSSColorValue = Color("#3730A3")

@Composable
fun MainPromoSections() {
    Column(
        gap = 20.px,
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
            padding(28.px, 32.px)
            backgroundColor(backgroundColor)
            borderRadius(12.px)
            property("box-shadow", "0 2px 12px rgba(0,0,0,0.08)")
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
            modifier = {
                width(100.percent)
            },
        ) {
            Column(
                gap = 4.px,
                modifier = {
                    flex(1)
                    property("min-width", "220px")
                    property("max-width", "360px")
                },
            ) {
                Span({
                    style {
                        applyTypography(CSSTypography.Styles.body)
                        fontSize(CSSTypography.FontSize.xxl)
                        fontWeight(CSSTypography.FontWeight.bold)
                        color(CSSColors.Black)
                        lineHeight("1.25")
                    }
                }) {
                    Text("Зачем вот это все?")
                }
                Span({
                    style {
                        applyTypography(CSSTypography.Styles.body)
                        fontSize(CSSTypography.FontSize.base)
                        color(CSSColors.Gray600)
                    }
                }) {
                    Text("Преимущества аренды авто от частных владельцев")
                }
                Div({
                    style {
                        marginTop(20.px)
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
private fun AdvantageGrid() {
    val items =
        listOf(
            "Стоит дешевле" to null,
            "Машины чище" to null,
            "Штрафов меньше" to "Частники не зарабатывают на страховании ответственности за повреждения и не штрафуют за ложные повреждения.",
            "Пробег меньше" to null,
            "Выбор больше" to null,
            "Оформить проще" to null,
        )
    Div({
        style {
            flex(2)
            property("min-width", "280px")
            display(DisplayStyle.Grid)
            property("grid-template-columns", "1fr 1fr")
            property("grid-template-rows", "1fr 1fr 1fr")
            property("grid-auto-flow", "column")
            property("gap", "10px")
        }
    }) {
        items.forEach { (label, tooltip) ->
            AdvantageChip(label = label, tooltip = tooltip)
        }
    }
}

@Composable
private fun AdvantageChip(
    label: String,
    tooltip: String?,
) {
    Button({
        tooltip?.let { attr("title", it) }
        style {
            padding(12.px, 16.px)
            backgroundColor(PromoIndigo)
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
