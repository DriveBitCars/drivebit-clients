package my.drivebit.screens

import androidx.compose.runtime.Composable
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
import my.drivebit.repositories.SelectedCityRepository
import my.drivebit.viewmodels.CityViewModel
import org.koin.compose.koinInject

@Composable
fun CitySelectionPage(onCitySelected: () -> Unit = {}) {
    val viewModel: CityViewModel = koinInject()
    val selectedCityRepository: SelectedCityRepository = koinInject()
    val cities by viewModel.cities.collectAsState()
    val error by viewModel.error.collectAsState()
    val query by viewModel.query.collectAsState()

    var inputValue by remember { mutableStateOf("") }

    PageWithLogo {
        CenteredFormContainer {
            PageHeader {
                TextSmartHeader("Выберите город")
            }

            FormSection {
                TextInputField(
                    label = "Город",
                    value = inputValue,
                    onValueChange = { newValue ->
                        inputValue = newValue
                        viewModel.updateQuery(newValue)
                    },
                )

                if (error != null) {
                    TextError(error ?: "Произошла ошибка")
                } else {
                    StringList(
                        strings = cities.map { it.name },
                        onSelected = { cityName ->
                            val city = cities.find { it.name == cityName }
                            city?.let {
                                inputValue = cityName
                                viewModel.clearQuery()
                                selectedCityRepository.saveCity(it.id, it.name)
                                onCitySelected()
                            }
                        },
                    )
                }
            }
        }
    }
}
