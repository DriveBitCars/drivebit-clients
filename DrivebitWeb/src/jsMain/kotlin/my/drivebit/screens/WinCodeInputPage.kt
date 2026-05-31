package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import my.drivebit.components.ActionButton
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.FormSection
import my.drivebit.components.PageHeader
import my.drivebit.shell.PageWithLogo
import my.drivebit.components.TextError
import my.drivebit.components.TextInputField
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.repositories.WinCodeRepository
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.ValidationState
import my.drivebit.viewmodels.WinCodeInputViewModel
import my.drivebit.viewmodels.createButtonViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.koin.compose.koinInject

@Composable
fun WinCodeInputPage(onWinCodeEntered: (String) -> Unit = {}) {
    val viewModel: WinCodeInputViewModel = koinInject()
    val winCodeRepository: WinCodeRepository = koinInject()
    val winCode by viewModel.winCode.collectAsState()
    val validationState by viewModel.validationState.collectAsState()
    val buttonViewModel = createButtonViewModel()

    LaunchedEffect(validationState) {
        buttonViewModel.setState(
            if (viewModel.isValid) {
                ButtonState.Enabled
            } else {
                ButtonState.Disabled
            },
        )
    }

    PageWithLogo {
        CenteredFormContainer {
            PageHeader {
                TextSmartHeader("Введите WIN код")
            }

            FormSection {
                TextInputField(
                    label = "WIN код",
                    value = winCode,
                    onValueChange = { newValue ->
                        viewModel.updateWinCode(newValue)
                    },
                    maxLength = 17,
                )

                if (validationState is ValidationState.Error) {
                    TextError((validationState as ValidationState.Error).message)
                }

                Div({
                    style {
                        display(DisplayStyle.Flex)
                        justifyContent(JustifyContent.Center)
                        marginTop(16.px)
                    }
                }) {
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
                                if (viewModel.isValid) {
                                    winCodeRepository.saveWinCode(winCode.trim().uppercase())
                                    onWinCodeEntered(winCode.trim().uppercase())
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
