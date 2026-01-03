package my.drivebit.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import my.drivebit.components.ActionButton
import my.drivebit.components.CenteredFormContainer
import my.drivebit.components.FormSection
import my.drivebit.components.PageHeader
import my.drivebit.components.PageWithLogo
import my.drivebit.components.TextError
import my.drivebit.components.TextInputField
import my.drivebit.components.TextSmartHeader
import my.drivebit.design.CSSColors
import my.drivebit.network.services.Car
import my.drivebit.network.services.CarCreateRequest
import my.drivebit.repositories.CarDataRepository
import my.drivebit.repositories.LicensePlateRepository
import my.drivebit.repositories.MyCarRepository
import my.drivebit.repositories.SelectedAddressRepository
import my.drivebit.repositories.SelectedBodyTypeRepository
import my.drivebit.repositories.SelectedCarBrandRepository
import my.drivebit.repositories.SelectedCarModelRepository
import my.drivebit.repositories.SelectedCityRepository
import my.drivebit.repositories.SelectedDriveTypeRepository
import my.drivebit.repositories.SelectedEngineTypeRepository
import my.drivebit.utils.safeLaunchWithErrorHandler
import my.drivebit.viewmodels.ButtonState
import my.drivebit.viewmodels.createButtonViewModel
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.koin.compose.koinInject

@Composable
fun DailyRateInputPage(onDailyRateEntered: () -> Unit = {}) {
    val carDataRepository: CarDataRepository = koinInject()
    val selectedCarBrandRepository: SelectedCarBrandRepository = koinInject()
    val selectedCarModelRepository: SelectedCarModelRepository = koinInject()
    val selectedBodyTypeRepository: SelectedBodyTypeRepository = koinInject()
    val selectedDriveTypeRepository: SelectedDriveTypeRepository = koinInject()
    val selectedEngineTypeRepository: SelectedEngineTypeRepository = koinInject()
    val licensePlateRepository: LicensePlateRepository = koinInject()
    val selectedAddressRepository: SelectedAddressRepository = koinInject()
    val selectedCityRepository: SelectedCityRepository = koinInject()
    val myCarRepository: MyCarRepository = koinInject()
    val carService: Car = koinInject()
    val buttonViewModel = createButtonViewModel()
    val coroutineScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    var dailyRate by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val isValid = dailyRate.toDoubleOrNull()?.let { it >= 0 } == true

    buttonViewModel.setState(
        if (isValid && !isLoading) {
            ButtonState.Enabled
        } else {
            ButtonState.Disabled
        },
    )

    fun createCar() {
        if (isLoading) return

        val brandId = selectedCarBrandRepository.getBrandId()
        val modelId = selectedCarModelRepository.getModelId()
        val bodyType = selectedBodyTypeRepository.getBodyTypeName()
        val driveType = selectedDriveTypeRepository.getDriveTypeName()
        val engineType = selectedEngineTypeRepository.getEngineTypeName()
        val engineVolume = carDataRepository.getEngineVolume()
        val productionYear = carDataRepository.getProductionYear()
        val seatsCount = carDataRepository.getSeatsCount()
        val licensePlate = licensePlateRepository.getLicensePlate()
        val address = selectedAddressRepository.getAddress()
        val addressData = selectedAddressRepository.getAddressData()
        val cityId = selectedCityRepository.getCityId()
        val hourlyRate = carDataRepository.getHourlyRate()
        val dailyRateValue = dailyRate.toDoubleOrNull() ?: 0.0

        println("🚗 [DailyRateInputPage] Creating car with data:")
        println("   - brandId: $brandId")
        println("   - modelId: $modelId")
        println("   - bodyType: $bodyType")
        println("   - driveType: $driveType")
        println("   - engineType: $engineType")
        println("   - engineVolume: $engineVolume")
        println("   - productionYear: $productionYear")
        println("   - seatsCount: $seatsCount")
        println("   - licensePlate: $licensePlate")
        println("   - address: $address")
        println("   - cityId: $cityId")
        println("   - addressData: $addressData")
        println("   - hourlyRate: $hourlyRate")
        println("   - dailyRate: $dailyRateValue")

        val request =
            CarCreateRequest(
                brandId = brandId,
                modelId = modelId,
                bodyType = bodyType,
                driveType = driveType,
                engineType = engineType,
                engineVolume = engineVolume,
                productionYear = productionYear,
                seatsCount = seatsCount,
                licensePlate = licensePlate,
                ValidAddressString = address ?: "",
                cityId = cityId?.toString(),
                addr = null,
                hourlyRate = hourlyRate ?: 0.0,
                dailyRate = dailyRateValue,
                ParkingAssistances = emptyList(),
                MultimediaSystemOptions = emptyList(),
            )

        coroutineScope.safeLaunchWithErrorHandler(
            isLoading = { isLoading },
            setLoading = { isLoading = it },
            setError = { error = it },
            errorHandler = { e -> e.message ?: "Не удалось создать автомобиль" },
        ) {
            val response =
                runCatching {
                    carService.createCar(request)
                }.getOrElse { e ->
                    println("❌ [DailyRateInputPage] Error creating car: ${e.message}")
                    throw e
                }

            val finalResponse =
                if (response.id.isBlank()) {
                    println("⚠️ [DailyRateInputPage] Car created but ID is empty, getting from repository...")
                    myCarRepository.refresh()
                    val cars = myCarRepository.getMyCar()
                    if (cars.isNotEmpty()) {
                        val firstCarId = cars.first().id
                        println("✅ [DailyRateInputPage] Found car ID from repository: $firstCarId")
                        my.drivebit.network.services
                            .CarResponse(id = firstCarId)
                    } else {
                        println("⚠️ [DailyRateInputPage] No cars found in repository, continuing with empty ID")
                        response
                    }
                } else {
                    response
                }

            println("✅ [DailyRateInputPage] Car creation successful, response.id: ${finalResponse.id}")
            myCarRepository.refresh()
            carDataRepository.saveCarId(finalResponse.id)
            onDailyRateEntered()
        }
    }

    PageWithLogo {
        CenteredFormContainer {
            PageHeader {
                TextSmartHeader("Оплата за день")
            }

            FormSection {
                TextInputField(
                    label = "Оплата за день (₽)",
                    value = dailyRate,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() || it == '.' || it == ',' }) {
                            dailyRate = newValue.replace(',', '.')
                            error = null
                        }
                    },
                    numeric = true,
                )

                if (error != null) {
                    TextError(error ?: "Произошла ошибка")
                }

                Div({
                    style {
                        display(DisplayStyle.Flex)
                        justifyContent(JustifyContent.Center)
                        marginTop(16.px)
                    }
                }) {
                    Div({
                        style {
                            maxWidth(200.px)
                            width(100.percent)
                        }
                    }) {
                        ActionButton(
                            viewModel = buttonViewModel,
                            enabledColor = CSSColors.Blue,
                            text = if (isLoading) "Создание..." else "Создать",
                            onClick = {
                                val rateValue = dailyRate.toDoubleOrNull()
                                if (rateValue != null && rateValue >= 0) {
                                    carDataRepository.saveDailyRate(rateValue)
                                    createCar()
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
