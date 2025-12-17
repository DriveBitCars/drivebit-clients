package my.drivebit.repositories

import my.drivebit.network.services.CarBrand
import my.drivebit.network.services.Dictionary

interface CarBrandRepository {
    suspend fun getBrands(): ResultCarBrands
}

sealed interface ResultCarBrands {
    data class Success(val brands: List<CarBrand>) : ResultCarBrands
    data class Error(val message: String) : ResultCarBrands
}

class CarBrandRepositoryImpl(
    private val dictionary: Dictionary,
) : CarBrandRepository {
    override suspend fun getBrands(): ResultCarBrands {
        return runCatching {
            dictionary.getCarBrands()
        }.fold(
            onSuccess = { brands -> ResultCarBrands.Success(brands) },
            onFailure = { exception ->
                ResultCarBrands.Error(exception.message ?: "Unknown error")
            },
        )
    }
}

