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
import my.drivebit.repositories.SelectedDriveTypeRepository
import my.drivebit.viewmodels.DriveTypeViewModel
import org.koin.compose.koinInject

@Composable
fun DriveTypeSelectionPage(onDriveTypeSelected: () -> Unit = {}) {
    val viewModel: DriveTypeViewModel = koinInject()
    val selectedDriveTypeRepository: SelectedDriveTypeRepository = koinInject()
    val driveTypes by viewModel.driveTypes.collectAsState()
    val viewModelError by viewModel.error.collectAsState()

    var inputValue by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        localError = null
        viewModel.loadDriveTypes()
    }

    val error = viewModelError ?: localError

    PageWithLogo {
        CenteredFormContainer {
            PageHeader {
                TextSmartHeader("Выберите привод")
            }

            FormSection(
                listingContent = {
                    if (error != null) {
                        TextError(error ?: "Произошла ошибка")
                    } else {
                        StringList(
                            strings = driveTypes.map { it.translate },
                            onSelected = { translate ->
                                val driveType = driveTypes.find { it.translate == translate }
                                driveType?.let {
                                    inputValue = translate
                                    viewModel.clearQuery()
                                    selectedDriveTypeRepository.saveDriveType(it.name, it.translate)
                                    onDriveTypeSelected()
                                }
                            },
                        )
                    }
                },
            ) {
                TextInputField(
                    label = "Привод",
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
