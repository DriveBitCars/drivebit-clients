package my.drivebit.repositories

import com.russhwolf.settings.Settings

interface SelectedEngineTypeRepository {
    fun saveEngineType(
        name: String,
        translate: String,
    )

    fun getEngineTypeName(): String?

    fun getEngineTypeTranslate(): String?

    fun clearEngineType()
}

internal class SelectedEngineTypeRepositoryImpl(
    private val settings: Settings,
) : SelectedEngineTypeRepository {
    companion object {
        private const val ENGINE_TYPE_NAME_KEY = "selected_engine_type_name"
        private const val ENGINE_TYPE_TRANSLATE_KEY = "selected_engine_type_translate"
    }

    override fun saveEngineType(
        name: String,
        translate: String,
    ) {
        settings.putString(ENGINE_TYPE_NAME_KEY, name)
        settings.putString(ENGINE_TYPE_TRANSLATE_KEY, translate)
    }

    override fun getEngineTypeName(): String? = settings.getStringOrNullIfEmpty(ENGINE_TYPE_NAME_KEY)

    override fun getEngineTypeTranslate(): String? = settings.getStringOrNullIfEmpty(ENGINE_TYPE_TRANSLATE_KEY)

    override fun clearEngineType() {
        settings.remove(ENGINE_TYPE_NAME_KEY)
        settings.remove(ENGINE_TYPE_TRANSLATE_KEY)
    }
}
