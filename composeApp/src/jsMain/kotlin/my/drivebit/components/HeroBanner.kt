package my.drivebit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import my.drivebit.design.CSSColors
import my.drivebit.design.CSSTypography
import my.drivebit.design.applyTypography
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.utils.heroBannerHeadline
import my.drivebit.viewmodels.DateFieldViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text

@Composable
fun HeroBanner(
    backgroundIconUrl: String,
    cityName: String,
    filterTitle: String,
    startDateViewModel: DateFieldViewModel,
    endDateViewModel: DateFieldViewModel,
) {
    val startState by startDateViewModel.state.collectAsState()
    val endState by endDateViewModel.state.collectAsState()
    val navigationController = LocalNavigationController.current

    val searchClick: () -> Unit = {
        val queryParams =
            buildString {
                if (startState.date != null) append("startDate=${startState.date}")
                if (endState.date != null) {
                    if (length > 0) append("&")
                    append("endDate=${endState.date}")
                }
            }
        val url = if (queryParams.isNotEmpty()) "/search?$queryParams" else "/search"
        navigationController?.navigateTo(url)
    }

    ResponsiveContainer { isMobile ->
        Div({
            style {
                width(100.percent)
                minHeight(if (isMobile) 360.px else 442.px)
                borderRadius(30.px)
                overflow("hidden")
                position(Position.Relative)
                marginBottom(30.px)
            }
        }) {
            Img(
                src = backgroundIconUrl,
                alt = "",
                attrs = {
                    style {
                        position(Position.Absolute)
                        top(0.px)
                        left(0.px)
                        width(100.percent)
                        height(100.percent)
                        display(DisplayStyle.Block)
                        property("object-fit", "cover")
                        property("object-position", "center")
                    }
                },
            )

            Div({
                style {
                    position(Position.Absolute)
                    top(0.px)
                    left(0.px)
                    right(0.px)
                    bottom(0.px)
                    background("linear-gradient(to bottom, rgba(9,5,43,0.5), rgba(9,5,43,0.7))")
                }
            })

            Div({
                style {
                    position(Position.Absolute)
                    top(0.px)
                    left(0.px)
                    right(0.px)
                    bottom(0.px)
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    alignItems(AlignItems.Center)
                    padding(if (isMobile) 20.px else 48.px, if (isMobile) 8.px else 100.px)
                    property("box-sizing", "border-box")
                }
            }) {
                Div({
                    style {
                        width(100.percent)
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                        alignItems(AlignItems.Center)
                        flex(1)
                        justifyContent(JustifyContent.Center)
                    }
                }) {
                    H1({
                        style {
                            applyTypography(CSSTypography.Styles.body)
                            fontSize(if (isMobile) 28.px else 48.px)
                            fontWeight(CSSTypography.FontWeight.semibold)
                            color(CSSColors.White)
                            textAlign("center")
                            lineHeight("1.2")
                            margin(0.px)
                            marginBottom(if (isMobile) 12.px else 20.px)
                        }
                    }) {
                        Text(heroBannerHeadline(cityName, filterTitle))
                    }

                    Div({
                        style {
                            applyTypography(CSSTypography.Styles.body)
                            fontSize(if (isMobile) 18.px else 32.px)
                            fontWeight(CSSTypography.FontWeight.medium)
                            color(CSSColors.White)
                            textAlign("center")
                            lineHeight("1.2")
                            marginBottom(if (isMobile) 8.px else 12.px)
                        }
                    }) {
                        Text("Чистые и ухоженные машины с низким пробегом")
                    }

                    Div({
                        style {
                            applyTypography(CSSTypography.Styles.body)
                            fontSize(if (isMobile) 20.px else 30.px)
                            property("font-weight", "800")
                            color(CSSColors.White)
                            textAlign("center")
                            lineHeight("1.2")
                            marginBottom(if (isMobile) 24.px else 48.px)
                        }
                    }) {
                        Text("Дешевле каршеринга")
                    }
                }

                if (isMobile) {
                    Column(
                        gap = 12.px,
                        modifier = {
                            width(100.percent)
                            property("max-width", "1000px")
                            property("margin", "0 auto")
                        },
                    ) {
                        Div({
                            style { width(100.percent) }
                        }) {
                            HeroDateRangeSelector(
                                startDateViewModel = startDateViewModel,
                                endDateViewModel = endDateViewModel,
                                compact = true,
                                onRangeConfirmed = searchClick,
                            )
                        }
                        Div({
                            style {
                                width(100.percent)
                                height(56.px)
                            }
                        }) {
                            HeroSearchButton(onClick = searchClick)
                        }
                    }
                } else {
                    Row(
                        gap = 8.px,
                        alignItems = AlignItems.Center,
                        modifier = {
                            width(100.percent)
                            property("max-width", "1000px")
                            property("margin", "0 auto")
                        },
                    ) {
                        Div({
                            style {
                                flex(1)
                                property("min-width", "0")
                            }
                        }) {
                            HeroDateRangeSelector(
                                startDateViewModel = startDateViewModel,
                                endDateViewModel = endDateViewModel,
                                compact = false,
                                onRangeConfirmed = searchClick,
                            )
                        }
                        Div({
                            style {
                                flexShrink(0)
                                width(292.px)
                                height(90.px)
                            }
                        }) {
                            HeroSearchButton(onClick = searchClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroSearchButton(onClick: () -> Unit) {
    org.jetbrains.compose.web.dom.Button({
        onClick { onClick() }
        style {
            width(100.percent)
            height(100.percent)
            borderRadius(20.px)
            backgroundColor(CSSColors.Blue)
            color(CSSColors.White)
            border(0.px)
            cursor("pointer")
            applyTypography(CSSTypography.Styles.button)
            fontSize(20.px)
            fontWeight(CSSTypography.FontWeight.semibold)
            property("transition", "background-color 0.2s ease")
        }
        onMouseEnter {
            (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                "background-color",
                "#1a4fd4",
            )
        }
        onMouseLeave {
            (it.target as? org.w3c.dom.HTMLElement)?.style?.setProperty(
                "background-color",
                CSSColors.BlueString,
            )
        }
    }) {
        Text("Найти автомобиль")
    }
}
