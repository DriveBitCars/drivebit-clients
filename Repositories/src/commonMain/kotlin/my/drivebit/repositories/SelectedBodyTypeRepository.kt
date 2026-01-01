package my.drivebit.repositories

import com.russhwolf.settings.Settings

interface SelectedBodyTypeRepository {
    fun saveBodyType(
        name: String,
        translate: String,
    )

    fun getBodyTypeName(): String?

    fun getBodyTypeTranslate(): String?

    fun clearBodyType()
}

internal class SelectedBodyTypeRepositoryImpl(
    private val settings: Settings,
) : SelectedBodyTypeRepository {
    companion object {
        private const val BODY_TYPE_NAME_KEY = "selected_body_type_name"
        private const val BODY_TYPE_TRANSLATE_KEY = "selected_body_type_translate"
    }

    override fun saveBodyType(
        name: String,
        translate: String,
    ) {
        settings.putString(BODY_TYPE_NAME_KEY, name)
        settings.putString(BODY_TYPE_TRANSLATE_KEY, translate)
    }

    override fun getBodyTypeName(): String? = settings.getStringOrNullIfEmpty(BODY_TYPE_NAME_KEY)

    override fun getBodyTypeTranslate(): String? = settings.getStringOrNullIfEmpty(BODY_TYPE_TRANSLATE_KEY)

    override fun clearBodyType() {
        settings.remove(BODY_TYPE_NAME_KEY)
        settings.remove(BODY_TYPE_TRANSLATE_KEY)
    }
}
