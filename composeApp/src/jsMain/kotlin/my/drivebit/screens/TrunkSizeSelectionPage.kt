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
import my.drivebit.components.PageHeader
import my.drivebit.components.PageWithLogo
import my.drivebit.components.StringList
import my.drivebit.components.TextError
import my.drivebit.components.TextInputField
import my.drivebit.components.TextSmartHeader
import my.drivebit.repositories.SelectedTrunkSizeRepository
import my.drivebit.viewmodels.TrunkSizeViewModel
import org.koin.compose.koinInject

@Composable
fun TrunkSizeSelectionPage(onTrunkSizeSelected: () -> Unit = {}) {
    val viewModel: TrunkSizeViewModel = koinInject()
    val selectedTrunkSizeRepository: SelectedTrunkSizeRepository = koinInject()
    val trunkSizes by viewModel.trunkSizes.collectAsState()
    val viewModelError by viewModel.error.collectAsState()

    var inputValue by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        localError = null
        viewModel.loadTrunkSizes()
    }

    val error = viewModelError ?: localError

    PageWithLogo {
        CenteredFormContainer {
            PageHeader {
                TextSmartHeader("Выберите размер бака")
            }

            FormSection(
                listingContent = {
                    if (error != null) {
                        TextError(error ?: "Произошла ошибка")
                    } else {
                        StringList(
                            strings = trunkSizes.map { it.translate },
                            onSelected = { translate ->
                                val trunk = trunkSizes.find { it.translate == translate }
                                trunk?.let {
                                    inputValue = translate
                                    viewModel.clearQuery()
                                    selectedTrunkSizeRepository.saveTrunkSize(it.name, it.translate)
                                    onTrunkSizeSelected()
                                }
                            },
                        )
                    }
                },
            ) {
                TextInputField(
                    label = "Размер багажника",
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
