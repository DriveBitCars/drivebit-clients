package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.Column
import my.drivebit.components.FormSection
import my.drivebit.components.Loader
import my.drivebit.components.PageHeader
import my.drivebit.components.PageWithLogo
import my.drivebit.components.Row
import my.drivebit.components.TextAreaField
import my.drivebit.components.TextError
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.navigation.LocalNavigationController
import my.drivebit.utils.getUrlParameter
import my.drivebit.viewmodels.CreateReviewViewModel
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexWrap
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexWrap
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.koin.compose.currentKoinScope
import org.koin.core.parameter.parametersOf

@Composable
fun LeaveReviewPage() {
    val carIdParam = getUrlParameter("carId")
    val navigationController = LocalNavigationController.current
    val koinScope = currentKoinScope()

    if (carIdParam.isBlank()) {
        PageWithLogo {
            CenteredFormContainer(maxWidth = 600.px) {
                PageHeader {
                    TextSmartHeader("Отзыв")
                }
                FormSection {
                    TextError("Не указан автомобиль")
                }
            }
        }
        return
    }

    val viewModel: CreateReviewViewModel =
        remember(carIdParam) {
            koinScope.get<CreateReviewViewModel> { parametersOf(carIdParam) }
        }
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            navigationController?.navigateTo("/my-bookings")
            viewModel.consumeSuccess()
        }
    }

    PageWithLogo {
        CenteredFormContainer(maxWidth = 600.px) {
            PageHeader {
                TextSmartHeader("Оставить отзыв")
            }

            FormSection {
                Column(gap = 20.px) {
                    Span({
                        style {
                            fontSize(14.px)
                            color(CSSColors.Gray600)
                        }
                    }) {
                        Text("Оцените поездку от 1 до 5")
                    }

                    Row(
                        gap = 8.px,
                        modifier = {
                            display(DisplayStyle.Flex)
                            flexWrap(FlexWrap.Wrap)
                            alignItems(AlignItems.Center)
                        },
                    ) {
                        for (star in 1..5) {
                            val selected = uiState.stars == star
                            Button({
                                style {
                                    property("min-width", 44.px.toString())
                                    padding(10.px, 14.px)
                                    borderRadius(8.px)
                                    border(1.px, LineStyle.Solid, if (selected) CSSColors.Blue else CSSColors.Gray300)
                                    backgroundColor(if (selected) CSSColors.Blue else CSSColors.White)
                                    color(if (selected) CSSColors.White else CSSColors.Black)
                                    fontSize(16.px)
                                    fontWeight("600")
                                    cursor("pointer")
                                }
                                onClick { viewModel.setStars(star) }
                            }) {
                                Text(star.toString())
                            }
                        }
                    }

                    TextAreaField(
                        label = "Комментарий (необязательно)",
                        value = uiState.text,
                        onValueChange = viewModel::setText,
                        maxLength = 1000,
                        rows = 5,
                    )

                    uiState.error?.let { err ->
                        TextError(err)
                    }

                    if (uiState.isSubmitting) {
                        Loader()
                    } else {
                        Button({
                            style {
                                marginTop(8.px)
                                width(100.percent)
                                padding(12.px, 24.px)
                                borderRadius(8.px)
                                border(0.px)
                                backgroundColor(CSSColors.Blue)
                                color(CSSColors.White)
                                fontSize(16.px)
                                fontWeight("600")
                                cursor("pointer")
                            }
                            onClick { viewModel.submit() }
                        }) {
                            Text("Отправить отзыв")
                        }
                    }
                }
            }
        }
    }
}
