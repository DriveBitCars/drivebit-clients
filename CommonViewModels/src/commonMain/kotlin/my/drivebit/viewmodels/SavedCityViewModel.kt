package my.drivebit.viewmodels

import my.drivebit.network.services.City
import my.drivebit.repositories.MyCityRepository
import my.drivebit.repositories.SelectedCityRepository

interface SavedCityViewModel {
    fun saveCity(city: City)
}

class SavedCityViewModelForCarCreation(
    private val selectedCityRepository: SelectedCityRepository,
) : SavedCityViewModel {
    override fun saveCity(city: City) {
        println("🏙️ [SavedCityViewModelForCarCreation] Сохранение города для создания машины:")
        println("   - city.id: ${city.id}")
        println("   - city.name: ${city.name}")
        selectedCityRepository.saveCity(city.id, city.name)
    }
}

class SavedCityViewModelForMyCity(
    private val myCityRepository: MyCityRepository,
) : SavedCityViewModel {
    override fun saveCity(city: City) {
        println("👤 [SavedCityViewModelForMyCity] Сохранение города для пользователя:")
        println("   - city.id: ${city.id}")
        println("   - city.name: ${city.name}")
        myCityRepository.selectCity(city.id, city.name)
    }
}
