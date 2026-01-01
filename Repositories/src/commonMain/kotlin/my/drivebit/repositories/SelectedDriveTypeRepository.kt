package my.drivebit.repositories

import com.russhwolf.settings.Settings

interface SelectedDriveTypeRepository {
    fun saveDriveType(
        name: String,
        translate: String,
    )

    fun getDriveTypeName(): String?

    fun getDriveTypeTranslate(): String?

    fun clearDriveType()
}

internal class SelectedDriveTypeRepositoryImpl(
    private val settings: Settings,
) : SelectedDriveTypeRepository {
    companion object {
        private const val DRIVE_TYPE_NAME_KEY = "selected_drive_type_name"
        private const val DRIVE_TYPE_TRANSLATE_KEY = "selected_drive_type_translate"
    }

    override fun saveDriveType(
        name: String,
        translate: String,
    ) {
        settings.putString(DRIVE_TYPE_NAME_KEY, name)
        settings.putString(DRIVE_TYPE_TRANSLATE_KEY, translate)
    }

    override fun getDriveTypeName(): String? = settings.getStringOrNullIfEmpty(DRIVE_TYPE_NAME_KEY)

    override fun getDriveTypeTranslate(): String? = settings.getStringOrNullIfEmpty(DRIVE_TYPE_TRANSLATE_KEY)

    override fun clearDriveType() {
        settings.remove(DRIVE_TYPE_NAME_KEY)
        settings.remove(DRIVE_TYPE_TRANSLATE_KEY)
    }
}
