package my.drivebit.search

import kotlinx.coroutines.flow.first
import my.drivebit.network.services.Dictionary
import my.drivebit.repositories.MyCityRepository
import my.drivebit.utils.cityNameToSlug
import my.drivebit.web.BrandSlugResolver
import org.koin.core.qualifier.named
import org.koin.dsl.module

val searchAppModule =
    module {
        single {
            val brandSlugResolver: BrandSlugResolver = get()
            val dictionary: Dictionary = get()
            val myCityRepository: MyCityRepository = get()
            SearchViewModel(
                repositoryFactory = { filters ->
                    SearchCarRepositoryImpl(
                        api = HttpSearchCarsApi(get(named("unauthorized"))),
                        filters = filters,
                        resolveCityId = {
                            myCityRepository.getSelectedCity.first().id.toString()
                        },
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
