package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.FormSection
import my.drivebit.shell.PageWithLogo
import my.drivebit.components.StringList
import my.drivebit.components.TextError
import my.drivebit.components.TextInputField
import my.drivebit.components.ToolbarBackArrow
import my.drivebit.repositories.SelectedEngineTypeRepository
import my.drivebit.viewmodels.EngineTypeViewModel
import org.koin.compose.koinInject

@Composable
fun EngineTypeSelectionPage(
    onEngineTypeSelected: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val viewModel: EngineTypeViewModel = koinInject()
    val selectedEngineTypeRepository: SelectedEngineTypeRepository = koinInject()
    val engineTypes by viewModel.engineTypes.collectAsState()
    val viewModelError by viewModel.error.collectAsState()

    var inputValue by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        localError = null
        viewModel.loadEngineTypes()
    }

    val error = viewModelError ?: localError

    PageWithLogo {
        CenteredFormContainer {
            ToolbarBackArrow(
                title = "Выберите тип двигателя",
                onBackClick = onBack,
            )

            FormSection(
                listingContent = {
                    if (error != null) {
                        TextError(error ?: "Произошла ошибка")
                    } else {
                        StringList(
                            strings = engineTypes.map { it.translate },
                            onSelected = { translate ->
                                val engineType = engineTypes.find { it.translate == translate }
                                engineType?.let {
                                    inputValue = translate
                                    viewModel.clearQuery()
                                    selectedEngineTypeRepository.saveEngineType(it.name, it.translate)
                                    onEngineTypeSelected()
                                }
                            },
                        )
                    }
                },
            ) {
                TextInputField(
                    label = "Тип двигателя",
                    value = inputValue,
                    onValueChange = { newValue ->
                        inputValue = newValue
                        viewModel.updateQuery(newValue)
                    },
                )
            }
        }
    }
}
