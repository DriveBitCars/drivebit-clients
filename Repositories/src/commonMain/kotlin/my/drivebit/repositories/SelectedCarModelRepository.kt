package my.drivebit.repositories

import com.russhwolf.settings.Settings

interface SelectedCarModelRepository {
    fun saveModel(
        modelId: Int,
        modelName: String,
    )

    fun getModelId(): Int?

    fun getModelName(): String?

    fun clearModel()
}

internal class SelectedCarModelRepositoryImpl(
    private val settings: Settings,
) : SelectedCarModelRepository {
    companion object {
        private const val MODEL_ID_KEY = "selected_car_model_id"
        private const val MODEL_NAME_KEY = "selected_car_model_name"
    }

    override fun saveModel(
        modelId: Int,
        modelName: String,
    ) {
        settings.putInt(MODEL_ID_KEY, modelId)
        settings.putString(MODEL_NAME_KEY, modelName)
    }

    override fun getModelId(): Int? {
        val modelId = settings.getInt(MODEL_ID_KEY, -1)
        return if (modelId == -1) null else modelId
    }

    override fun getModelName(): String? = settings.getStringOrNullIfEmpty(MODEL_NAME_KEY)

    override fun clearModel() {
        settings.remove(MODEL_ID_KEY)
        settings.remove(MODEL_NAME_KEY)
    }
}
