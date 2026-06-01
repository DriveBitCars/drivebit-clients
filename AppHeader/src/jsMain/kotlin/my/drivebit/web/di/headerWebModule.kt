package my.drivebit.web.di

import my.drivebit.navigation.NavigationState
import org.koin.dsl.module

val headerWebModule =
    module {
        single { NavigationState() }
    }
