package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.shared.storage.Storage
import my.drivebit.utils.REDIRECT_PATH
import my.drivebit.utils.encodeUrlParameter
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.koinInject

private val PromoBlueBg: CSSColorValue = Color("#E3F2FD")
private val PromoChipBlue: CSSColorValue = Color("#4338CA")
private val PromoRectButtonBlue: CSSColorValue = Color("#2962FF")

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
        ResponsiveContainer { isMobile ->
            if (isMobile) {
                Column(
                    gap = 32.px,
                    modifier = { width(100.percent) },
                ) {
                    WhySectionLeftContent(isMobile = true)
                    AdvantageGrid(isMobile = true)
                }
            } else {
                Row(
                    gap = 32.px,
                    flexWrap = FlexWrap.Wrap,
                    alignItems = AlignItems.Stretch,
                    modifier = {
                        width(100.percent)
                    },
                ) {
                    WhySectionLeftContent(isMobile = false)
                    AdvantageGrid(isMobile = false)
                }
            }
        }
    }
}

@Composable
private fun WhySectionLeftContent(isMobile: Boolean) {
    Column(
        gap = 6.px,
        modifier = {
            if (isMobile) {
                width(100.percent)
            } else {
                flex(1)
                property("min-height", "100%")
                property("align-self", "stretch")
            }
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
                if (isMobile) {
                    width(100.percent)
                } else {
                    property("align-self", "flex-start")
                }
            }
        }) {
            PromoRectButton(
                text = "Арендовать автомобиль",
                onClick = { window.location.href = "/search" },
                compact = isMobile,
            )
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
                color(CSSColors.Black)
            }
        }) {
            Text(subtitle)
        }
    }
}

private val ADVANTAGES =
    listOf(
        "Стоит дешевле" to
            "Стоимость аренды от частного владельца при длительных сроках значительно дешевле. Экономия может составлять 30–40%.",
        "Машины чище" to
            "Авто сдаются от случая к случаю, а не постоянно. Собственник значительно лучше следит за состоянием своей машины.",
        "Пробег меньше" to
            "Автомобили не используются в коммерческих целях на постоянной основе, дальние поездки на таких машинах организуются нечасто.",
        "Выбор больше" to "Так как сервис объединяет разных собственников, выбор машин значительно расширяется.",
        "Оформить проще" to
            "Максимально простое оформление аренды — регистрируетесь в сервисе, выбираете автомобиль, подаёте заявку на аренду.",
    )

@Composable
private fun AdvantageGrid(isMobile: Boolean) {
    Div({
        style {
            if (!isMobile) {
                flex(2)
                property("min-width", "250px")
                display(DisplayStyle.Grid)
                property("grid-template-columns", "1fr 1fr")
                property("row-gap", "68px")
                property("column-gap", "40px")
            } else {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                gap(16.px)
                width(100.percent)
            }
        }
    }) {
        ADVANTAGES.forEach { (title, fullText) ->
            AdvantageChip(shortTitle = title, fullText = fullText, isMobile = isMobile)
        }
    }
}

@Composable
private fun AdvantageChip(
    shortTitle: String,
    fullText: String,
    isMobile: Boolean,
) {
    var hovered by remember { mutableStateOf(false) }
    val chipHeight = if (isMobile) 120.px else 150.px
    val chipWidth = if (isMobile) 100.percent else 330.px
    val fontSizeTitle = if (isMobile) CSSTypography.FontSize.lg else CSSTypography.FontSize.xxl
    val fontSizeBack = CSSTypography.FontSize.sm

    Div({
        style {
            height(chipHeight)
            if (isMobile) width(100.percent) else width(chipWidth)
            property("perspective", "1000px")
            property("box-sizing", "border-box")
            cursor("pointer")
        }
        onMouseEnter { hovered = true }
        onMouseLeave { hovered = false }
    }) {
        Div({
            style {
                position(Position.Relative)
                width(100.percent)
                height(100.percent)
                property("transform-style", "preserve-3d")
                property("transition", "transform 0.5s ease")
                if (hovered) {
                    property("transform", "rotateY(180deg)")
                }
            }
        }) {
            Div({
                style {
                    position(Position.Absolute)
                    left(0.px)
                    top(0.px)
                    width(100.percent)
                    height(100.percent)
                    padding(14.px, 18.px)
                    backgroundColor(Color("#09052B"))
                    color(CSSColors.White)
                    borderRadius(20.px)
                    property("box-sizing", "border-box")
                    property("box-shadow", "0 4px 12px rgba(0,0,0,0.15)")
                    property("backface-visibility", "hidden")
                    property("-webkit-backface-visibility", "hidden")
                    property("display", "flex")
                    property("align-items", "center")
                    property("justify-content", "center")
                    property("text-align", "center")
                }
            }) {
                Span({
                    style {
                        applyTypography(CSSTypography.Styles.body)
                        fontSize(fontSizeTitle)
                        fontWeight(CSSTypography.FontWeight.semibold)
                        lineHeight("1.25")
                    }
                }) {
                    Text(shortTitle)
                }
            }
            Div({
                style {
                    position(Position.Absolute)
                    left(0.px)
                    top(0.px)
                    width(100.percent)
                    height(100.percent)
                    padding(14.px, 18.px)
                    backgroundColor(CSSColors.White)
                    color(CSSColors.Black)
                    borderRadius(20.px)
                    property("box-sizing", "border-box")
                    property("box-shadow", "0 4px 12px rgba(0,0,0,0.15)")
                    property("backface-visibility", "hidden")
                    property("-webkit-backface-visibility", "hidden")
                    property("transform", "rotateY(180deg)")
                    property("display", "flex")
                    property("align-items", "center")
                    property("justify-content", "center")
                    property("overflow-y", "auto")
                }
            }) {
                Span({
                    style {
                        applyTypography(CSSTypography.Styles.body)
                        fontSize(fontSizeBack)
                        lineHeight("1.4")
                        property("text-align", "center")
                    }
                }) {
                    Text(fullText)
                }
            }
        }
    }
}

@Composable
private fun EarnSectionCard() {
    val storage: Storage = koinInject()
    PromoCard(backgroundColor = CSSColors.White) {
        ResponsiveContainer { isMobile ->
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
                        fontSize(if (isMobile) CSSTypography.FontSize.xl else CSSTypography.FontSize.xxl)
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
                        fontSize(CSSTypography.FontSize.sm)
                        color(CSSColors.Gray600)
                        property("text-align", "center")
                        property("max-width", "480px")
                        lineHeight("1.5")
                    }
                }) {
                    Text(
                        "Подайте заявку на регистрацию в сервисе и сдавайте свой автомобиль на время пока им не пользуетесь.",
                    )
                }
                Div({
                    style {
                        marginTop(20.px)
                        width(100.percent)
                        property("max-width", "280px")
                    }
                }) {
                    PromoRectButton(
                        text = "Сдать автомобиль",
                        compact = isMobile,
                        onClick = {
                            if (storage.isLogined()) {
                                window.location.href = "/list-your-car.html"
                            } else {
                                val redirect = "/list-your-car.html".encodeUrlParameter()
                                window.location.href = "/login-by-phone?$REDIRECT_PATH=$redirect"
                            }
                        },
                    )
                }
            }
        }
    }
}
