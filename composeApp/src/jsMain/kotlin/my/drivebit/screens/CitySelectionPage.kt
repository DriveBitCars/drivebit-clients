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
import my.drivebit.shell.PageWithLogo
import my.drivebit.components.StringList
import my.drivebit.components.TextError
import my.drivebit.components.TextInputField
import my.drivebit.components.TextSmartHeader
import my.drivebit.network.services.City
import my.drivebit.repositories.MyCityRepository
import my.drivebit.repositories.SelectedCityRepository
import my.drivebit.viewmodels.CityViewModel
import my.drivebit.viewmodels.SavedCityViewModel
import my.drivebit.viewmodels.SavedCityViewModelForCarCreation
import my.drivebit.viewmodels.SavedCityViewModelForMyCity
import org.koin.compose.koinInject

sealed interface CitySelectionMode {
    data class ForCarCreation(
        val onCitySelected: () -> Unit,
    ) : CitySelectionMode

    data class ForMyCity(
        val onCitySelected: (City) -> Unit,
    ) : CitySelectionMode
}

@Composable
fun CitySelectionPage(mode: CitySelectionMode) {
    val viewModel: CityViewModel = koinInject()
    val selectedCityRepository: SelectedCityRepository = koinInject()
    val myCityRepository: MyCityRepository = koinInject()

    val savedCityViewModel: SavedCityViewModel =
        remember(mode) {
            when (mode) {
                is CitySelectionMode.ForCarCreation -> SavedCityViewModelForCarCreation(selectedCityRepository)
                is CitySelectionMode.ForMyCity -> SavedCityViewModelForMyCity(myCityRepository)
            }
        }

    val browseAllCities = mode is CitySelectionMode.ForMyCity
    LaunchedEffect(browseAllCities) {
        if (browseAllCities) {
            viewModel.enableBrowseAllCitiesMode()
        } else {
            viewModel.setSearchOnlyMode()
        }
    }

    val cities by viewModel.cities.collectAsState()
    val error by viewModel.error.collectAsState()
    val query by viewModel.query.collectAsState()

    var inputValue by remember { mutableStateOf("") }

    PageWithLogo {
        CenteredFormContainer {
            PageHeader {
                TextSmartHeader("Выберите город")
            }

            FormSection(
                listingContent = {
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
                                    savedCityViewModel.saveCity(it)
                                    when (mode) {
                                        is CitySelectionMode.ForCarCreation -> mode.onCitySelected()
                                        is CitySelectionMode.ForMyCity -> mode.onCitySelected(it)
                                    }
                                }
                            },
                        )
                    }
                },
            ) {
                TextInputField(
                    label = "Город",
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
