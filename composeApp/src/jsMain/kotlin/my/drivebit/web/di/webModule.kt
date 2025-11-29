package my.drivebit.web.di

import my.drivebit.navigation.NavigationState
import my.drivebit.network.di.networkModule
import org.koin.dsl.module

val webModule =
    module {
        includes(networkModule)
        single { NavigationState() }
    }
