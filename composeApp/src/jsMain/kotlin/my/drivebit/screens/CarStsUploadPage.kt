package my.drivebit.screens

import androidx.compose.runtime.Composable
import kotlinx.browser.window
import my.drivebit.components.ActionButton
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.FormSection
import my.drivebit.components.PageHeader
import my.drivebit.components.PageWithLogo
import my.drivebit.components.Row
import my.drivebit.components.TextError
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.utils.getUrlParameter
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.maxWidth
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

@Composable
fun CarStsUploadPage(
    onContinue: () -> Unit = {
        window.location.href = "/my-cars"
    },
) {
    val carId = getUrlParameter("carId")

    PageWithLogo {
        CenteredFormContainer {
            PageHeader {
                TextSmartHeader("СТС автомобиля")
            }

            FormSection {
                if (carId.isBlank()) {
                    TextError("Не указан ID автомобиля")
                } else {
                    Div({
                        style {
                            marginTop(0.px)
                            marginBottom(16.px)
                            fontSize(14.px)
                            color(CSSColors.Gray600)
                        }
                    }) {
                        Text(
                            "Загрузите обе стороны свидетельства о регистрации. " +
                                "Можно пропустить и добавить позже в разделе «Управление автомобилем».",
                        )
                    }

                    CarStsDocumentsSection(carId = carId)

                    Row(
                        justifyContent = JustifyContent.Center,
                        modifier = { marginTop(24.px) },
                    ) {
                        Div({
                            style {
                                maxWidth(240.px)
                                width(100.percent)
                            }
                        }) {
                            ActionButton(
                                enabledColor = CSSColors.Blue,
                                text = "Продолжить",
                                onClick = onContinue,
                            )
                        }
                    }
                }
            }
        }
    }
}
