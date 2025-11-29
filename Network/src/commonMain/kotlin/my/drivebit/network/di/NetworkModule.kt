package my.drivebit.network.di

import my.drivebit.network.createHttpClientWithConfig
import my.drivebit.network.services.Auth
import my.drivebit.network.services.AuthImpl
import org.koin.dsl.module

/**
 * Network module for Koin DI
 * API Base URL: https://api.drivebit.my/api
 * API Documentation: https://api.drivebit.my:5000/swagger
 */
val networkModule =
    module {
        single {
            createHttpClientWithConfig()
        }
        single<Auth> {
            AuthImpl(get())
        }
    }
