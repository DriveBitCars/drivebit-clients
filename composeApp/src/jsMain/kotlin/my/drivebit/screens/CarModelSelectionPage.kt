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
import my.drivebit.repositories.SelectedCarModelRepository
import my.drivebit.utils.getUrlParameter
import my.drivebit.viewmodels.CarModelViewModel
import org.koin.compose.koinInject

@Composable
fun CarModelSelectionPage(
    onModelSelected: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val viewModel: CarModelViewModel = koinInject()
    val selectedCarModelRepository: SelectedCarModelRepository = koinInject()
    val models by viewModel.models.collectAsState()
    val viewModelError by viewModel.error.collectAsState()

    var inputValue by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val brandIdParam = getUrlParameter("brandId")
        val brandId = brandIdParam.toIntOrNull()
        if (brandId != null) {
            localError = null
            viewModel.loadModels(brandId, existingInFleetOnly = true)
        } else {
            localError = "Бренд не выбран"
        }
    }

    val error = viewModelError ?: localError

    PageWithLogo {
        CenteredFormContainer {
            ToolbarBackArrow(
                title = "Выберите модель",
                onBackClick = onBack,
            )

            FormSection(
                listingContent = {
                    if (error != null) {
                        TextError(error ?: "Произошла ошибка")
                    } else {
                        StringList(
                            strings = models.map { it.name },
                            onSelected = { modelName ->
                                val model = models.find { it.name == modelName }
                                model?.let {
                                    inputValue = modelName
                                    viewModel.clearQuery()
                                    selectedCarModelRepository.saveModel(it.id, it.name)
                                    onModelSelected()
                                }
                            },
                        )
                    }
                },
            ) {
                TextInputField(
                    label = "Модель",
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
