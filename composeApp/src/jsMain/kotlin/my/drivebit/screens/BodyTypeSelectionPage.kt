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
import my.drivebit.repositories.SelectedBodyTypeRepository
import my.drivebit.viewmodels.BodyTypeViewModel
import org.koin.compose.koinInject

@Composable
fun BodyTypeSelectionPage(onBodyTypeSelected: () -> Unit = {}) {
    val viewModel: BodyTypeViewModel = koinInject()
    val selectedBodyTypeRepository: SelectedBodyTypeRepository = koinInject()
    val bodyTypes by viewModel.bodyTypes.collectAsState()
    val viewModelError by viewModel.error.collectAsState()

    var inputValue by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        localError = null
        viewModel.loadBodyTypes()
    }

    val error = viewModelError ?: localError

    PageWithLogo {
        CenteredFormContainer {
            PageHeader {
                TextSmartHeader("Выберите тип кузова")
            }

            FormSection(
                listingContent = {
                if (error != null) {
                    TextError(error ?: "Произошла ошибка")
                } else {
                    StringList(
                        strings = bodyTypes.map { it.translate },
                        onSelected = { translate ->
                            val bodyType = bodyTypes.find { it.translate == translate }
                            bodyType?.let {
                                inputValue = translate
                                viewModel.clearQuery()
                                selectedBodyTypeRepository.saveBodyType(it.name, it.translate)
                                onBodyTypeSelected()
                            }
                        },
                    )
                }
                },
            ) {
                TextInputField(
                    label = "Тип кузова",
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
