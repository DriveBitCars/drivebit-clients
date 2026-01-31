package my.drivebit.repositories

import com.russhwolf.settings.Settings

interface SelectedTrunkSizeRepository {
    fun saveTrunkSize(
        name: String,
        translate: String,
    )

    fun getTrunkSizeName(): String?

    fun getTrunkSizeTranslate(): String?

    fun clearTrunkSize()
}

internal class SelectedTrunkSizeRepositoryImpl(
    private val settings: Settings,
) : SelectedTrunkSizeRepository {
    companion object {
        private const val TRUNK_SIZE_NAME_KEY = "selected_trunk_size_name"
        private const val TRUNK_SIZE_TRANSLATE_KEY = "selected_trunk_size_translate"
    }

    override fun saveTrunkSize(
        name: String,
        translate: String,
    ) {
        settings.putString(TRUNK_SIZE_NAME_KEY, name)
        settings.putString(TRUNK_SIZE_TRANSLATE_KEY, translate)
    }

    override fun getTrunkSizeName(): String? = settings.getStringOrNullIfEmpty(TRUNK_SIZE_NAME_KEY)

    override fun getTrunkSizeTranslate(): String? = settings.getStringOrNullIfEmpty(TRUNK_SIZE_TRANSLATE_KEY)

    override fun clearTrunkSize() {
        settings.remove(TRUNK_SIZE_NAME_KEY)
        settings.remove(TRUNK_SIZE_TRANSLATE_KEY)
    }
}
