package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import my.drivebit.components.ActionButton
import my.drivebit.components.ButtonContainer
import my.drivebit.components.CarTravelDestinationsField
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.FormSection
import my.drivebit.components.TextError
import my.drivebit.components.TextSmallBodyBlack
import my.drivebit.components.ToolbarBackArrow
import my.drivebit.design.CSSColors
import my.drivebit.shell.PageWithLogo
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.TravelDestinationsViewModel
import my.drivebit.viewmodels.createButtonViewModel
import org.koin.compose.koinInject

@Composable
fun TravelDestinationsSelectionPage(
    onContinue: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val viewModel: TravelDestinationsViewModel = koinInject()
    val options by viewModel.options.collectAsState()
    val selectedNames by viewModel.selectedNames.collectAsState()
    val error by viewModel.error.collectAsState()
    val continueButtonViewModel = createButtonViewModel()

    LaunchedEffect(Unit) {
        viewModel.load()
        continueButtonViewModel.setState(ButtonState.Enabled)
    }

    PageWithLogo {
        CenteredFormContainer {
            ToolbarBackArrow(
                title = "Направления путешествий",
                onBackClick = onBack,
            )

            FormSection {
                TextSmallBodyBlack("Можно выбрать несколько направлений или пропустить")
                if (error != null) {
                    TextError(error ?: "Произошла ошибка")
                } else {
                    CarTravelDestinationsField(
                        options = options,
                        selectedNames = selectedNames,
                        onToggle = { viewModel.toggle(it) },
                    )
                }
            }

            ButtonContainer(id = "travel-destinations-continue") {
                ActionButton(
                    enabledColor = CSSColors.Blue,
                    text = "Далее",
                    viewModel = continueButtonViewModel,
                    onClick = {
                        viewModel.confirmSelection()
                        onContinue()
                    },
                )
            }
        }
    }
}
