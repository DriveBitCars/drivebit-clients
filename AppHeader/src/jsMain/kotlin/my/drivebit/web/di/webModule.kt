package my.drivebit.web.di

import my.drivebit.maps.di.mapsModule
import my.drivebit.network.di.networkModule
import my.drivebit.network.services.Dictionary
import my.drivebit.web.BrandSlugResolver
import my.drivebit.web.CitySlugResolver
import org.koin.dsl.module

val webExtensionModule =
    module {
        includes(networkModule)
        includes(mapsModule)
        single { CitySlugResolver(get<Dictionary>()) }
        single { BrandSlugResolver(get<Dictionary>()) }
    }
