package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.Column
import my.drivebit.components.FormSection
import my.drivebit.components.PageHeader
import my.drivebit.shell.PageWithLogo
import my.drivebit.components.StringList
import my.drivebit.components.TextInputField
import my.drivebit.components.TextSmallBodyGray
import my.drivebit.components.TextSmartHeader
import my.drivebit.repositories.SelectedAddressRepository
import my.drivebit.viewmodels.AddressSuggestViewModel
import org.jetbrains.compose.web.css.px
import org.koin.compose.koinInject

@Composable
fun AddressInputPage(
    onAddressSelected: (String) -> Unit = {},
    onNavigateToWinCode: () -> Unit = {},
) {
    val viewModel: AddressSuggestViewModel = koinInject()
    val selectedAddressRepository: SelectedAddressRepository = koinInject()
    // val selectedCityRepository: SelectedCityRepository = koinInject()
    // Выбор города при создании машины отключён: город вводится вместе с адресом
    // selectedCityRepository.getCityName() ?: return

    val suggestions by viewModel.suggestions.collectAsState()

    var inputValue by remember { mutableStateOf("") }

    PageWithLogo {
        CenteredFormContainer {
            PageHeader {
                Column(gap = 8.px, marginBottom = 8.px) {
                    TextSmartHeader("Добавление автомобиля")
                    TextSmallBodyGray("Шаг 1 · Адрес, где находится автомобиль")
                    TextSmallBodyGray(
                        "Укажите адрес, по которому арендаторы смогут забрать машину. " +
                            "Начните вводить улицу и выберите вариант из списка.",
                    )
                }
            }

            FormSection(
                listingContent = {
                    StringList(
                        strings = suggestions.mapNotNull { it.value },
                        onSelected = { suggestionString ->
                            val suggestion = suggestions.firstOrNull { it.value == suggestionString }
                            inputValue = suggestionString
                            viewModel.clearSuggestions()
                            selectedAddressRepository.saveAddress(suggestionString)
                            suggestion?.data?.let { addressData ->
                                selectedAddressRepository.saveAddressData(addressData)
                            }
                            onAddressSelected(suggestionString)
                            onNavigateToWinCode()
                        },
                    )
                },
            ) {
                TextInputField(
                    label = "Адрес автомобиля",
                    placeholder = "Например: Москва, ул. Тверская, 10",
                    value = inputValue,
                    onValueChange = { newValue ->
                        inputValue = newValue
                        if (newValue.isNotBlank() && newValue.isNotEmpty()) {
                            viewModel.updateQuery(newValue)
                        }
                    },
                )
            }
        }
    }
}
