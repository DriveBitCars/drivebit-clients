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
import my.drivebit.repositories.SelectedCarBrandRepository
import my.drivebit.viewmodels.CarBrandViewModel
import org.koin.compose.koinInject

@Composable
fun CarBrandSelectionPage(
    onBrandSelected: (brandId: Int) -> Unit = {},
    onBack: () -> Unit = {},
) {
    val viewModel: CarBrandViewModel = koinInject()
    val selectedCarBrandRepository: SelectedCarBrandRepository = koinInject()
    val brands by viewModel.brands.collectAsState()
    val error by viewModel.error.collectAsState()

    var inputValue by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadBrands()
    }

    PageWithLogo {
        CenteredFormContainer {
            ToolbarBackArrow(
                title = "Выберите бренд",
                onBackClick = onBack,
            )

            FormSection(
                listingContent = {
                    if (error != null) {
                        TextError(error ?: "Произошла ошибка")
                    } else {
                        StringList(
                            strings = brands.map { it.name },
                            onSelected = { brandName ->
                                val brand = brands.find { it.name == brandName }
                                brand?.let {
                                    inputValue = brandName
                                    viewModel.clearQuery()
                                    selectedCarBrandRepository.saveBrand(it.id, it.name)
                                    onBrandSelected(it.id)
                                }
                            },
                        )
                    }
                },
            ) {
                TextInputField(
                    label = "Бренд",
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
