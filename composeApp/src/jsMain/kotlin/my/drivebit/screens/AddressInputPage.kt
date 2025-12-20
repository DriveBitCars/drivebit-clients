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
import my.drivebit.components.TextInputField
import my.drivebit.components.TextSmartHeader
import my.drivebit.repositories.SelectedAddressRepository
import my.drivebit.repositories.SelectedCityRepository
import my.drivebit.viewmodels.AddressSuggestViewModel
import org.koin.compose.koinInject

@Composable
fun AddressInputPage(
    onAddressSelected: (String) -> Unit = {},
    onNavigateToWinCode: () -> Unit = {},
) {
    val viewModel: AddressSuggestViewModel = koinInject()
    val selectedAddressRepository: SelectedAddressRepository = koinInject()
    val selectedCityRepository: SelectedCityRepository = koinInject()

    selectedCityRepository.getCityName() ?: return

    val suggestions by viewModel.suggestions.collectAsState()

    var inputValue by remember { mutableStateOf("") }

    PageWithLogo {
        CenteredFormContainer {
            PageHeader {
                TextSmartHeader("Введите адрес")
            }

            FormSection {
                TextInputField(
                    label = "Адрес",
                    value = inputValue,
                    onValueChange = { newValue ->
                        inputValue = newValue
                        if (newValue.isNotBlank() && newValue.isNotEmpty()) {
                            viewModel.updateQuery(newValue)
                        }
                    },
                )

                StringList(
                    strings = suggestions,
                    onSelected = { suggestion ->
                        inputValue = suggestion
                        viewModel.clearSuggestions()
                        selectedAddressRepository.saveAddress(suggestion)
                        onAddressSelected(suggestion)
                        onNavigateToWinCode()
                    },
                )
            }
        }
    }
}
