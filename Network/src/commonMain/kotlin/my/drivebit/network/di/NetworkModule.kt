package my.drivebit.network.di

import my.drivebit.network.api.ApiClient
import my.drivebit.network.createHttpClientWithConfig
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

        single {
            ApiClient(
                httpClient = get(),
                baseUrl = "https://api.drivebit.my/api",
            )
        }
    }
