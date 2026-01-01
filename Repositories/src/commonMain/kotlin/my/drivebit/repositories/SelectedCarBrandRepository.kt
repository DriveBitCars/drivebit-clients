package my.drivebit.repositories

import com.russhwolf.settings.Settings

interface SelectedCarBrandRepository {
    fun saveBrand(
        brandId: Int,
        brandName: String,
    )

    fun getBrandId(): Int?

    fun getBrandName(): String?

    fun clearBrand()
}

internal class SelectedCarBrandRepositoryImpl(
    private val settings: Settings,
) : SelectedCarBrandRepository {
    companion object {
        private const val BRAND_ID_KEY = "selected_car_brand_id"
        private const val BRAND_NAME_KEY = "selected_car_brand_name"
    }

    override fun saveBrand(
        brandId: Int,
        brandName: String,
    ) {
        settings.putInt(BRAND_ID_KEY, brandId)
        settings.putString(BRAND_NAME_KEY, brandName)
    }

    override fun getBrandId(): Int? {
        val brandId = settings.getInt(BRAND_ID_KEY, -1)
        return if (brandId == -1) null else brandId
    }

    override fun getBrandName(): String? = settings.getStringOrNullIfEmpty(BRAND_NAME_KEY)

    override fun clearBrand() {
        settings.remove(BRAND_ID_KEY)
        settings.remove(BRAND_NAME_KEY)
    }
}
