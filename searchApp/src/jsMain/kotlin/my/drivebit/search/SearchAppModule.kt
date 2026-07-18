package my.drivebit.search

import my.drivebit.network.services.Dictionary
import my.drivebit.utils.cityNameToSlug
import my.drivebit.web.BrandSlugResolver
import org.koin.core.qualifier.named
import org.koin.dsl.module

val searchAppModule =
    module {
        single {
            val brandSlugResolver: BrandSlugResolver = get()
            val dictionary: Dictionary = get()
            SearchViewModel(
                repositoryFactory = { filters ->
                    SearchCarRepositoryImpl(
                        api = HttpSearchCarsApi(get(named("unauthorized"))),
                        filters = filters,
                        cityIdResolver = ::resolveSearchCityId,
                    )
                },
                brandResolver = { slug ->
                    brandSlugResolver.resolve(slug)?.let { it.id to it.name }
                },
                modelResolver = { brandId, modelSlug ->
                    dictionary
                        .getCarModelsExisting(brandId)
                        .firstOrNull { cityNameToSlug(it.name) == modelSlug.lowercase() }
                        ?.let { it.id to it.name }
                },
            )
        }
    }
