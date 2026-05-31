package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import my.drivebit.components.ActionButton
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.FormSection
import my.drivebit.shell.PageWithLogo
import my.drivebit.components.Row
import my.drivebit.components.TextAreaField
import my.drivebit.components.ToolbarBackArrow
import my.drivebit.design.CSSColors
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.DescriptionInputViewModel
import my.drivebit.viewmodels.createButtonViewModel
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.maxWidth
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.koin.compose.koinInject

@Composable
fun DescriptionInputPage(
    onDescriptionEntered: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val viewModel: DescriptionInputViewModel = koinInject()
    val state by viewModel.state.collectAsState()
    val buttonViewModel = createButtonViewModel()

    buttonViewModel.setState(
        if (state.canSubmit) ButtonState.Enabled else ButtonState.Disabled,
    )

    PageWithLogo {
        CenteredFormContainer {
            ToolbarBackArrow(
                title = "Описание автомобиля",
                onBackClick = onBack,
            )

            FormSection {
                TextAreaField(
                    label = "Описание",
                    value = state.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    maxLength = 1000,
                    rows = 6,
                )

                Row(
                    justifyContent = JustifyContent.Center,
                    modifier = { marginTop(16.px) },
                ) {
                    Div({
                        style {
                            maxWidth(200.px)
                            width(100.percent)
                        }
                    }) {
                        ActionButton(
                            viewModel = buttonViewModel,
                            enabledColor = CSSColors.Blue,
                            text = "Продолжить",
                            onClick = {
                                if (viewModel.submit()) {
                                    onDescriptionEntered()
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
