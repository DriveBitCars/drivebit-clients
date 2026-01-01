package my.drivebit.repositories

import my.drivebit.network.services.CarModel
import my.drivebit.network.services.Dictionary

interface CarModelRepository {
    suspend fun getModels(brandId: Int): ResultCarModels
}

sealed interface ResultCarModels {
    data class Success(
        val models: List<CarModel>,
    ) : ResultCarModels

    data class Error(
        val message: String,
    ) : ResultCarModels
}

class CarModelRepositoryImpl(
    private val dictionary: Dictionary,
) : CarModelRepository {
    override suspend fun getModels(brandId: Int): ResultCarModels =
        runCatching {
            dictionary.getCarModels(brandId)
        }.fold(
            onSuccess = { models -> ResultCarModels.Success(models) },
            onFailure = { exception ->
                ResultCarModels.Error(exception.message ?: "Unknown error")
            },
        )
}
