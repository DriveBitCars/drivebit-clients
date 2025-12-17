package my.drivebit.repositories

interface SelectedCarBrandRepository {
    fun saveBrand(brandId: Int, brandName: String)
    fun getBrandId(): Int?
    fun getBrandName(): String?
    fun clearBrand()
}

class SelectedCarBrandRepositoryImpl : SelectedCarBrandRepository {
    private var brandId: Int? = null
    private var brandName: String? = null

    override fun saveBrand(brandId: Int, brandName: String) {
        this.brandId = brandId
        this.brandName = brandName
    }

    override fun getBrandId(): Int? = brandId

    override fun getBrandName(): String? = brandName

    override fun clearBrand() {
        brandId = null
        brandName = null
    }
}

