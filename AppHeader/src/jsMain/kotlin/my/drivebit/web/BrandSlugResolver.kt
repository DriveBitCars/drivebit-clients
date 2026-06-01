package my.drivebit.web

import my.drivebit.network.services.CarBrand
import my.drivebit.network.services.Dictionary
import my.drivebit.utils.cityNameToSlug

data class ResolvedBrand(
    val id: Int,
    val name: String,
    val slug: String,
)

class BrandSlugResolver(
    private val dictionary: Dictionary,
) {
    private var slugToBrand: Map<String, ResolvedBrand>? = null

    private suspend fun ensureLoaded() {
        if (slugToBrand != null) return
        val brands = dictionary.getCarBrandsExisting().sortedBy { it.id }
        slugToBrand =
            buildMap {
                for (brand in brands) {
                    val slug = cityNameToSlug(brand.name)
                    if (slug !in this) {
                        put(slug, brand.toResolved(slug))
                    }
                }
            }
    }

    suspend fun resolve(slug: String): ResolvedBrand? {
        ensureLoaded()
        return slugToBrand!![slug.lowercase()]
    }

    suspend fun allBrands(): List<ResolvedBrand> {
        ensureLoaded()
        return slugToBrand!!.values.sortedBy { it.name }
    }

    private fun CarBrand.toResolved(slug: String): ResolvedBrand =
        ResolvedBrand(
            id = id,
            name = name,
            slug = slug,
        )
}
