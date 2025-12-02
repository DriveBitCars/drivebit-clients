package my.drivebit.network.di

import my.drivebit.network.createHttpClientWithConfig
import my.drivebit.network.services.Auth
import my.drivebit.network.services.AuthImpl
import org.koin.dsl.module

/**
 * Network module for Koin DI
 * API Base URL: https://api.drivebit.my/api
 * API Documentation: http://213.171.27.185:5000/swagger/index.html
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
