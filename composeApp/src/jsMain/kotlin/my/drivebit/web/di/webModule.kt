package my.drivebit.web.di

import my.drivebit.navigation.NavigationState
import my.drivebit.network.di.networkModule
import my.drivebit.network.services.Dictionary
import my.drivebit.web.BrandSlugResolver
import my.drivebit.web.CitySlugResolver
import org.koin.dsl.module

val webModule =
    module {
        includes(networkModule)
        single { NavigationState() }
        single { CitySlugResolver(get<Dictionary>()) }
        single { BrandSlugResolver(get<Dictionary>()) }
    }
